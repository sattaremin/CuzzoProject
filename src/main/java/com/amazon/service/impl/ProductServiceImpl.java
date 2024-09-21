package com.amazon.service.impl;

import com.amazon.dto.ClientVendorDto;
import com.amazon.dto.ProductDto;
import com.amazon.entity.*;
import com.amazon.enums.ProductUnit;
import com.amazon.exception.ProductNotFoundException;
import com.amazon.repository.ClientVendorRepository;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.ProductService;
import com.amazon.service.SecurityService;
import com.amazon.util.MapperUtil;
import com.amazon.util.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MapperUtil mapperUtil;
    private final SecurityService securityService;
    private final ClientVendorRepository clientVendorRepository;
    private final InvoiceProductRepository invoiceProductRepository;


    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, MapperUtil mapperUtil, SecurityService securityService, ClientVendorRepository clientVendorRepository, InvoiceProductRepository invoiceProductRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.mapperUtil = mapperUtil;
        this.securityService = securityService;
        this.clientVendorRepository = clientVendorRepository;
        this.invoiceProductRepository = invoiceProductRepository;
    }

    @Override
    public List<ProductDto> listAllProductsByCategoryAndProductName() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .filter(product -> Objects.equals(securityService.getCurrentUserCompany().getId(),
                        product.getCategory().getCompany().getId()))
                .filter(product -> product.getQuantityInStock() >= 1)
                .filter(product -> !product.getIsDeleted())
                .map(product -> {
                    ProductDto dto = mapperUtil.convert(product, new ProductDto());

                    dto.setHasInvoiceProduct(hasInvoice(product.getId()));
                    return dto;
                })
                .sorted(Comparator.<ProductDto, String>comparing(productDto -> productDto.getCategory().getDescription())
                        .thenComparing(ProductDto::getName))
                .toList();
    }

    @Override
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::convertToDto)
                .orElse(null);
    }

    @Override
    public Boolean validStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);

        return product != null && product.getQuantityInStock() >= quantity;

    }

    @Override
    public List<ProductDto> findAvailableProductsForInvoice(Long invoiceId) {
        List<Product> allProducts = productRepository.findAllByQuantityInStockGreaterThan(0);
        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(invoiceId);

        Map<Long, Integer> productQuantitiesInInvoice = invoiceProducts.stream()
                .collect(Collectors.groupingBy(ip -> ip.getProduct().getId(), Collectors.summingInt(InvoiceProduct::getQuantity)));

        return allProducts.stream()
                .filter(product -> product.getQuantityInStock() > productQuantitiesInInvoice.getOrDefault(product.getId(), 0))
                .map(product -> mapperUtil.convert(product, new ProductDto()))
                .collect(Collectors.toList());
    }

    @Override
    public void create(ProductDto productDto) {
        Product product = productMapper.convertToEntity(productDto);
        productRepository.save(product);

    }


    @Override
    public Product findByName(String name, ProductUnit productUnit, Category category, int lowLimitAlert) {
        return productRepository.findByNameAndProductUnitAndCategoryAndLowLimitAlert(name, productUnit, category, lowLimitAlert);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));

        if (hasInvoice(id)) {
            throw new IllegalArgumentException("Cannot be deleted! This product has invoice(s).");
        }

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    public void updateOrCreate(ProductDto productDto) {

        Product existingProduct = productRepository.findByNameAndProductUnitAndCategoryAndLowLimitAlert(productDto.getName(), productDto.getProductUnit(), productMapper.convertToEntity(productDto).getCategory(), productDto.getLowLimitAlert());

        if (existingProduct != null) {
            existingProduct.setQuantityInStock(existingProduct.getQuantityInStock() + 1);

            productRepository.save(existingProduct);
        } else {

            if (productDto.getQuantityInStock() == null || productDto.getQuantityInStock() < 1) {
                productDto.setQuantityInStock(1);
            }
            Product product = productMapper.convertToEntity(productDto);
            productRepository.save(product);
        }
    }


    @Override
    public List<ProductDto> listProductsInStock() {
        List<Product> productsInStock = productRepository.findProductsInStock();
        return productsInStock.stream().map(productMapper::convertToDto).collect(Collectors.toList());

    }


    @Override
    public void edit(ProductDto product) {

        Product product1 = productRepository.findById(product.getId()).orElseThrow();

        Product convertedProduct = productMapper.convertToEntity(product);

        convertedProduct.setQuantityInStock(product1.getQuantityInStock());

        productRepository.save(convertedProduct);


    }

    @Override
    public boolean hasInvoice(Long productId) {

        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) {
            return false;
        }

        if (product.get().getQuantityInStock() > 1) {
            return true;
        }

        return invoiceProductRepository.existsInvoiceWithProduct(productId);
    }

    @Override
    public List<ClientVendorDto> findAllVendors() {
        List<ClientVendor> vendors = clientVendorRepository.findAll();
        return vendors.stream()
                .map(vendor -> mapperUtil.convert(vendor, new ClientVendorDto()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> findAllInStockProducts() {
        List<Product> products = productRepository.findAllByQuantityInStockGreaterThan(0);
        return products.stream()
                .map(product -> mapperUtil.convert(product, new ProductDto()))
                .collect(Collectors.toList());
    }

}