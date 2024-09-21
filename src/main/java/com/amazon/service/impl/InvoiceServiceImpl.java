package com.amazon.service.impl;

import com.amazon.annotation.ExecutionTime;
import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.dto.ProductDto;
import com.amazon.dto.UserDto;
import com.amazon.entity.*;
import com.amazon.enums.InvoiceStatus;
import com.amazon.enums.InvoiceType;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.repository.ClientVendorRepository;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.InvoiceService;
import com.amazon.service.ProductService;
import com.amazon.service.SecurityService;
import com.amazon.util.InvoiceProductMapper;
import com.amazon.util.MapperUtil;
import com.amazon.util.ProductMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {


    private final InvoiceRepository invoiceRepository;
    private final MapperUtil mapperUtil;
    private final SecurityService securityService;
    private final InvoiceProductRepository invoiceProductRepository;
    private final InvoiceProductMapper invoiceProductMapper;
    private final ProductMapper productMapper;
    private final InvoiceProductService invoiceProductService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    private final ClientVendorRepository clientVendorRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, MapperUtil mapperUtil,
                              SecurityService securityService, InvoiceProductRepository invoiceProductRepository, InvoiceProductMapper invoiceProductMapper, ProductMapper productMapper, @Lazy InvoiceProductService invoiceProductService, ProductService productService, ProductRepository productRepository, ClientVendorRepository clientVendorRepository) {
        this.invoiceRepository = invoiceRepository;
        this.mapperUtil = mapperUtil;
        this.securityService = securityService;
        this.invoiceProductRepository = invoiceProductRepository;
        this.invoiceProductMapper = invoiceProductMapper;

        this.productMapper = productMapper;
        this.invoiceProductService = invoiceProductService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.clientVendorRepository = clientVendorRepository;
    }


    @Override
    public InvoiceDto findById(Long id) throws InvoiceNotFoundException {

        return mapperUtil.convert(invoiceRepository.findById(id).orElseThrow(() ->
                new InvoiceNotFoundException("Invoice Not Found")), new InvoiceDto());
    }

    public List<InvoiceDto> listAllInvoices(InvoiceType invoiceType) {

        UserDto loggedInUser = securityService.getLoggedInUser();
        String companyTitle = loggedInUser.getCompany().getTitle();

        List<Invoice> invoices = invoiceRepository
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(invoiceType, companyTitle);

        List<InvoiceDto> invoiceDtoList = invoices.stream()
                .map(invoice -> {
                    InvoiceDto invoiceDto = mapperUtil.convert(invoice, new InvoiceDto());

                    Optional<InvoiceProduct> optionalInvoiceProduct = invoiceProductRepository.findById(invoice.getId());

                    optionalInvoiceProduct.ifPresent(invoiceProduct -> {
                        int quantity = invoiceProduct.getQuantity();
                        BigDecimal priceTotal = invoiceProduct.getPrice().multiply(BigDecimal.valueOf(quantity));
                        invoiceDto.setPrice(priceTotal);

                        BigDecimal tax = BigDecimal.valueOf(invoiceProduct.getTax());
                        BigDecimal totalTax = tax.multiply(priceTotal).divide(BigDecimal.valueOf(100));
                        invoiceDto.setTax(totalTax);

                        invoiceDto.setTotal(priceTotal.add(totalTax));
                    });

                    return invoiceDto;
                })
                .toList();

        return invoiceDtoList;
    }

    @Override
    public List<InvoiceDto> listAllPurchaseInvoices() {
        UserDto loggedInUser = securityService.getLoggedInUser();
        String companyTitle = loggedInUser.getCompany().getTitle();

        List<Invoice> invoices = invoiceRepository
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.PURCHASE, companyTitle);

        List<InvoiceDto> invoiceDtoList = invoices.stream()
                .map(invoice -> mapperUtil.convert(invoice, new InvoiceDto()))
                .collect(Collectors.toList());

        invoiceDtoList = invoiceDtoList.stream().map(invoiceDto -> {
            Long invoiceId = invoiceDto.getId();
            List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(invoiceId);

            if (invoiceProducts.isEmpty()) {
                invoiceDto.setPrice(BigDecimal.ZERO);
                invoiceDto.setTax(BigDecimal.ZERO);
                invoiceDto.setTotal(BigDecimal.ZERO);
            } else {
                BigDecimal totalPrice = BigDecimal.ZERO;
                BigDecimal totalTax = BigDecimal.ZERO;

                for (InvoiceProduct invoiceProduct : invoiceProducts) {
                    int quantity = invoiceProduct.getQuantity();
                    BigDecimal priceTotal = invoiceProduct.getPrice().multiply(BigDecimal.valueOf(quantity));
                    BigDecimal tax = BigDecimal.valueOf(invoiceProduct.getTax());
                    BigDecimal productTotalTax = tax.multiply(priceTotal).divide(BigDecimal.valueOf(100));

                    totalPrice = totalPrice.add(priceTotal);
                    totalTax = totalTax.add(productTotalTax);
                }

                invoiceDto.setPrice(totalPrice);
                invoiceDto.setTax(totalTax);
                invoiceDto.setTotal(totalPrice.add(totalTax));
            }

            return invoiceDto;
        }).collect(Collectors.toList());

        return invoiceDtoList;
    }

    @Override
    public List<InvoiceDto> listAllSalesInvoices() {
        UserDto loggedInUser = securityService.getLoggedInUser();
        String companyTitle = loggedInUser.getCompany().getTitle();

        List<Invoice> invoices = invoiceRepository
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.SALES, companyTitle);

        List<InvoiceDto> invoiceDtoList = invoices.stream()
                .map(invoice -> mapperUtil.convert(invoice, new InvoiceDto()))
                .collect(Collectors.toList());

        invoiceDtoList = invoiceDtoList.stream().map(invoiceDto -> {
            Long invoiceId = invoiceDto.getId();
            List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllInvoiceProductsByInvoiceId(invoiceId);

            if (invoiceProducts.isEmpty()) {
                invoiceDto.setPrice(BigDecimal.ZERO);
                invoiceDto.setTax(BigDecimal.ZERO);
                invoiceDto.setTotal(BigDecimal.ZERO);
            } else {
                BigDecimal totalPrice = BigDecimal.ZERO;
                BigDecimal totalTax = BigDecimal.ZERO;

                for (InvoiceProduct invoiceProduct : invoiceProducts) {
                    int quantity = invoiceProduct.getQuantity();
                    BigDecimal priceTotal = invoiceProduct.getPrice().multiply(BigDecimal.valueOf(quantity));
                    BigDecimal tax = BigDecimal.valueOf(invoiceProduct.getTax());
                    BigDecimal productTotalTax = tax.multiply(priceTotal).divide(BigDecimal.valueOf(100));

                    totalPrice = totalPrice.add(priceTotal);
                    totalTax = totalTax.add(productTotalTax);
                }

                invoiceDto.setPrice(totalPrice);
                invoiceDto.setTax(totalTax);
                invoiceDto.setTotal(totalPrice.add(totalTax));
            }

            return invoiceDto;
        }).collect(Collectors.toList());

        return invoiceDtoList;
    }


    @Override
    public void save(InvoiceDto invoiceDto, InvoiceType invoiceType) {
        Invoice invoice = mapperUtil.convert(invoiceDto, new Invoice());
        invoice.setInvoiceType(invoiceType);
        invoice.setDate(LocalDate.now());
        invoice.setInvoiceNo(newInvoiceNo(invoiceType));
        invoice.setInvoiceStatus(InvoiceStatus.AWAITING_APPROVAL);

        Company company = mapperUtil.convert(securityService.getCurrentUserCompany(), new Company());
        invoice.setCompany(company);

        // Sørg for at ClientVendor er satt
        if (invoiceDto.getClientVendor() == null || invoiceDto.getClientVendor().getId() == null) {
            throw new IllegalArgumentException("ClientVendor must not be null");
        }

        ClientVendor clientVendor = clientVendorRepository.findById(invoiceDto.getClientVendor().getId())
                .orElseThrow(() -> new EntityNotFoundException("ClientVendor not found with ID: " + invoiceDto.getClientVendor().getId()));
        invoice.setClientVendor(clientVendor);

        invoiceRepository.save(invoice);
    }




    @Override
    public String newInvoiceNo(InvoiceType invoiceType) {
        Long companyId = securityService.getCurrentUserCompany().getId();
        String invoiceNo;
        if (invoiceType == InvoiceType.PURCHASE) {
            invoiceNo = listAllPurchaseInvoice(companyId).isEmpty() ? "P-000" : listAllPurchaseInvoice(companyId).get(listAllPurchaseInvoice(companyId).size() - 1);
        } else {
            invoiceNo = listAllSalesInvoice(companyId).isEmpty() ? "S-000" : listAllSalesInvoice(companyId).get(listAllSalesInvoice(companyId).size() - 1);
        }
        int i = Integer.parseInt(invoiceNo.substring(2));
        return String.format("%s-%03d", invoiceType == InvoiceType.PURCHASE ? "P" : "S", i + 1);
    }

    List<String> listAllPurchaseInvoice(Long companyId) {
        return invoiceRepository.findAllByInvoiceTypeAndCompany_Id(InvoiceType.PURCHASE, companyId).stream()
                .map(p -> p.getInvoiceNo().replace("P-", ""))
                .sorted()
                .toList();
    }

    List<String> listAllSalesInvoice(Long companyId) {
        return invoiceRepository.findAllByInvoiceTypeAndCompany_Id(InvoiceType.SALES, companyId).stream()
                .map(p -> p.getInvoiceNo().replace("S-", ""))
                .sorted()
                .toList();
    }
    @Override
    public void removeInvoiceById(Long id) {
        invoiceRepository.deleteById(id);
    }

    @ExecutionTime
    @Override
    public void approvePurchaseInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found with invoiceId: " + invoiceId));
        List<InvoiceProductDto> invoiceProducts = invoiceProductService.findAllByInvoiceIdAndIsDeleted(invoiceId,false);



        for (InvoiceProductDto invoiceProduct : invoiceProducts) {

            ProductDto productDto = productService.getProductById(invoiceProduct.getProduct().getId());
            productDto.setQuantityInStock(productDto.getQuantityInStock() + invoiceProduct.getQuantity());

            invoiceProduct.setRemainingQuantity(invoiceProduct.getQuantity());
            invoiceProduct.setProfitLoss(BigDecimal.ZERO);
            invoiceProductService.saveInvoiceProduct(invoiceProduct,invoiceId);

            productRepository.save(mapperUtil.convert(productDto,new Product()));

        }
        invoice.setInvoiceStatus(InvoiceStatus.APPROVED);
        invoiceRepository.save(invoice);
        invoice.setDate(LocalDate.now());

    }

    @Override
    public void approveSalesInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found with invoiceId: " + invoiceId));
        List<InvoiceProductDto> invoiceProducts = invoiceProductService.findAllByInvoiceIdAndIsDeleted(invoiceId, false);

        for (InvoiceProductDto invoiceProduct : invoiceProducts) {
            ProductDto productDto = productService.getProductById(invoiceProduct.getProduct().getId());


            if (invoiceProduct.getPrice() == null) {
                throw new IllegalStateException("Invoice product price not found for invoice product ID: " + invoiceProduct.getId());
            }

            if (productDto.getUnitPrice() == null) {
                productDto.setUnitPrice(BigDecimal.ZERO);
            }

            productDto.setQuantityInStock(productDto.getQuantityInStock() - invoiceProduct.getQuantity());

            invoiceProduct.setRemainingQuantity(0);
            BigDecimal profitLoss = invoiceProduct.getPrice().subtract(productDto.getUnitPrice())
                    .multiply(BigDecimal.valueOf(invoiceProduct.getQuantity()));
            invoiceProduct.setProfitLoss(profitLoss);

            invoiceProductService.saveInvoiceProduct(invoiceProduct, invoiceId);
            productRepository.save(mapperUtil.convert(productDto, new Product()));
        }

        invoice.setInvoiceStatus(InvoiceStatus.APPROVED);
        invoiceRepository.save(invoice);
        invoice.setDate(LocalDate.now());
    }


    @Override
    public void updateInvoice(InvoiceDto invoiceDto) {
        Invoice invoice = mapperUtil.convert(invoiceDto, new Invoice());
        invoiceRepository.save(invoice);
    }


    @Override
    public void removeInvoiceProduct(Long invoiceId, Long invoiceProductId) {
        InvoiceProduct invoiceProduct = invoiceProductRepository.findById(invoiceProductId)
                .orElseThrow(() -> new RuntimeException("Invoice Product not found"));
        invoiceProductRepository.delete(invoiceProduct);
    }

    @Override
    public List<InvoiceProductDto> findAllInvoiceProductsByInvoiceId(Long invoiceId) {
        return invoiceProductRepository.findAllByInvoiceId(invoiceId).stream()
                .map(invoiceProduct -> mapperUtil.convert(invoiceProduct, new InvoiceProductDto()))
                .collect(Collectors.toList());
    }

    @Override
    public void calculateTotalWithTax(InvoiceProduct invoiceProduct) {
        BigDecimal totalPrice = invoiceProduct.getPrice().multiply(BigDecimal.valueOf(invoiceProduct.getQuantity()));
        BigDecimal totalTax = totalPrice.multiply(BigDecimal.valueOf(invoiceProduct.getTax())).divide(BigDecimal.valueOf(100));
        invoiceProduct.setTotalWithTax(totalPrice.add(totalTax));
    }

    @Transactional
    @Override
    public void addInvoiceProduct(Long invoiceId, InvoiceProductDto newInvoiceProductDto) throws InvoiceNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
        InvoiceProduct invoiceProduct = mapperUtil.convert(newInvoiceProductDto, new InvoiceProduct());
        invoiceProduct.setId(null);
        invoiceProduct.setInvoice(invoice);
        calculateTotalWithTax(invoiceProduct);
        invoiceProductRepository.save(invoiceProduct);

    }

}















