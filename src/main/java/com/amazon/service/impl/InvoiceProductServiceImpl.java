package com.amazon.service.impl;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;

import com.amazon.entity.Invoice;
import com.amazon.entity.InvoiceProduct;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.entity.Product;
import com.amazon.exception.InsufficientStockException;
import com.amazon.enums.InvoiceStatus;

import com.amazon.exception.InvoiceProductNotFoundException;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.InvoiceService;
import com.amazon.service.SecurityService;
import com.amazon.util.InvoiceMapper;
import com.amazon.util.InvoiceProductMapper;
import com.amazon.util.MapperUtil;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceProductServiceImpl implements InvoiceProductService {

    private final InvoiceProductRepository invoiceProductRepository;
    private final MapperUtil mapperUtil;

    private final InvoiceProductMapper invoiceProductMapper;

    private final InvoiceRepository invoiceRepository;

    private final ProductRepository productRepository;

    private final InvoiceService invoiceService;

    private final SecurityService securityService;

    private final InvoiceMapper invoiceMapper;

    public InvoiceProductServiceImpl(InvoiceProductRepository invoiceProductRepository, MapperUtil mapperUtil, InvoiceProductMapper invoiceProductMapper, InvoiceRepository invoiceRepository, ProductRepository productRepository, InvoiceService invoiceService, SecurityService securityService, InvoiceMapper invoiceMapper) {
        this.invoiceProductRepository = invoiceProductRepository;
        this.mapperUtil = mapperUtil;
        this.invoiceProductMapper = invoiceProductMapper;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
        this.securityService = securityService;
        this.invoiceMapper = invoiceMapper;

    }

    @Override
    public InvoiceProductDto findById(Long id) throws InvoiceProductNotFoundException {

        return mapperUtil.convert(invoiceProductRepository.findById(id).orElseThrow(() ->
                new InvoiceProductNotFoundException("InvoiceProduct with id " + id + " not found")), new InvoiceProductDto());
    }

    @Override
    public List<InvoiceProductDto> listAllByInvoiceId(Long invoiceId) {
        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.retrieveAllByInvoice_IdAndIsDeletedFalse(invoiceId);
        return invoiceProducts.stream()
                .map(invoiceProduct -> {
                    InvoiceProductDto invoiceProductDto = mapperUtil.convert(invoiceProduct, new InvoiceProductDto());
                    if (invoiceProductDto.getQuantity() == null || invoiceProductDto.getPrice() == null || invoiceProductDto.getTax() == null) {
                        throw new IllegalArgumentException("Quantity, Price, and Tax must not be null");
                    }
                    invoiceProductDto.setTotal(calculateInvoiceProductTotal(invoiceProductDto));
                    return invoiceProductDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateInvoiceProductTotal(InvoiceProductDto invoiceProductDto) {
        if (invoiceProductDto.getQuantity() == null || invoiceProductDto.getPrice() == null || invoiceProductDto.getTax() == null) {
            throw new IllegalArgumentException("Quantity, Price, and Tax must not be null");
        }
        BigDecimal price = invoiceProductDto.getPrice();
        int quantity = invoiceProductDto.getQuantity();
        BigDecimal taxRate = BigDecimal.valueOf(invoiceProductDto.getTax()).divide(BigDecimal.valueOf(100)); // 10%

        BigDecimal total = price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.add(taxRate))
                .setScale(2, RoundingMode.HALF_UP);

        return total;
    }

    @Override
    public InvoiceProductDto setInvoiceTotal(InvoiceProductDto invoiceProductDto) {
        BigDecimal total = calculateInvoiceProductTotal(invoiceProductDto);
        invoiceProductDto.setTotal(total);
        return invoiceProductDto;
    }

    @Override
    public InvoiceDto calculateAndSetInvoiceTotals(Long invoiceId) {
        CompanyDto currentCompany = securityService.getCurrentUserCompany();
        Long companyId = currentCompany.getId();

        InvoiceStatus invoiceStatus = InvoiceStatus.APPROVED;

        Invoice invoice = invoiceRepository.findByIdAndCompanyIdAndInvoiceStatus(invoiceId, companyId, invoiceStatus);

        if (invoice == null) {
            throw new EntityNotFoundException("No approved invoice found for ID: " + invoiceId + " and Company ID: " + companyId);
        }

        InvoiceDto invoiceDto = invoiceMapper.convertToDto(invoice);


        List<InvoiceProductDto> invoiceProducts = listAllByInvoiceId(invoiceId).stream()
                .map(this::setInvoiceTotal)
                .collect(Collectors.toList());

        BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalSubtotalAmount = BigDecimal.ZERO;


        for (InvoiceProductDto product : invoiceProducts) {
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
            BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(product.getTax()).divide(BigDecimal.valueOf(100)));
            totalSubtotalAmount = totalSubtotalAmount.add(subtotal);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
            totalInvoiceAmount = totalInvoiceAmount.add(product.getTotal());
        }

        invoiceDto.setPrice(totalSubtotalAmount.setScale(2, RoundingMode.HALF_UP));
        invoiceDto.setTax(totalTaxAmount.setScale(2, RoundingMode.HALF_UP));
        invoiceDto.setTotal(totalInvoiceAmount.setScale(2, RoundingMode.HALF_UP));

        return invoiceDto;
    }


    @Override
    public BigDecimal calculateTotal(BigDecimal price, Integer quantity, Integer tax) {


        // Convert tax percentage to a multiplier
        BigDecimal taxRate = BigDecimal.valueOf(tax).divide(BigDecimal.valueOf(100));
        BigDecimal taxMultiplier = taxRate.add(BigDecimal.ONE);

        // Calculate subtotal (price * quantity)
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

        // Calculate total (subtotal * taxMultiplier)
        BigDecimal total = subtotal.multiply(taxMultiplier);

        return total;
    }

    @Override
    public List<InvoiceProduct> findApprovedInvoices() {
        return invoiceProductRepository.findApprovedInvoices(InvoiceStatus.APPROVED);
    }


    public List<InvoiceProductDto> getActiveProductsByInvoiceId(Long invoiceId) {
        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findActiveProductsByInvoiceId(invoiceId);

        return invoiceProducts.stream().map(invoiceProductMapper::convertToDto).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void addInvoiceProduct(Long invoiceId, InvoiceProductDto newInvoiceProductDto) throws
            InvoiceNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        Product product = productRepository.findById(newInvoiceProductDto.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id"));

        if (product.getQuantityInStock() < newInvoiceProductDto.getQuantity()) {
            throw new InsufficientStockException("Insufficient quantity in stock");
        }

        InvoiceProduct invoiceProduct = mapperUtil.convert(newInvoiceProductDto, new InvoiceProduct());
        invoiceProduct.setId(null);
        invoiceProduct.setInvoice(invoice);

        if (invoiceProduct.getPrice() == null) {
            throw new IllegalArgumentException("Price must not be null");
        }
        BigDecimal total = calculateTotal(invoiceProduct.getPrice(), invoiceProduct.getQuantity(), invoiceProduct.getTax());
        invoiceProduct.setTotalWithTax(total);

        invoiceProductRepository.save(invoiceProduct);


        product.setQuantityInStock(product.getQuantityInStock() - newInvoiceProductDto.getQuantity());
        productRepository.save(product);
    }


    @Override
    public List<InvoiceProductDto> findAllByInvoiceIdAndIsDeleted(Long id, boolean isDeleted) {
        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceIdAndIsDeleted(id, isDeleted);
        return invoiceProducts.stream()
                .map(invoiceProduct -> {
                    InvoiceProductDto dto = mapperUtil.convert(invoiceProduct, new InvoiceProductDto());
                    if (dto.getPrice() == null) dto.setPrice(BigDecimal.ZERO);
                    if (dto.getQuantity() == null) dto.setQuantity(0);
                    if (dto.getTax() == null) dto.setTax(0);
                    BigDecimal totalPrice = calculateTotal(dto.getPrice(), dto.getQuantity(), dto.getTax());
                    dto.setTotal(totalPrice);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public BigDecimal calculateTotal(BigDecimal price, Integer quantity, BigDecimal tax) {
        BigDecimal totalWithoutTax = price.multiply(new BigDecimal(quantity));
        BigDecimal totalTax = totalWithoutTax.multiply(tax.divide(new BigDecimal("100")));
        return totalWithoutTax.add(totalTax);

    }


    public void saveInvoiceProduct(InvoiceProductDto invoiceProductDto, Long id) {
        InvoiceProduct invoiceProduct = mapperUtil.convert(invoiceProductDto, new InvoiceProduct());
        InvoiceDto invoiceDto = invoiceService.findById(id);
        if (invoiceDto == null) {
            throw new IllegalArgumentException("Invalid invoice Id");
        }

        if (invoiceProductDto.getProduct() == null || invoiceProductDto.getProduct().getId() == null) {
            throw new IllegalArgumentException("Invalid product Id");
        }

        if (invoiceProductDto.getQuantity() == null || invoiceProductDto.getPrice() == null) {
            throw new IllegalArgumentException("Quantity and Price must not be null");
        }
        Invoice invoice = mapperUtil.convert(invoiceDto, new Invoice());

        invoiceProduct.setInvoice(invoice);

        invoiceProductRepository.save(invoiceProduct);

    }


}
