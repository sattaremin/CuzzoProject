package com.amazon.service.impl;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.dto.ProductDto;
import com.amazon.entity.Invoice;
import com.amazon.entity.InvoiceProduct;
import com.amazon.entity.Product;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.InvoiceService;
import com.amazon.service.SecurityService;
import com.amazon.util.InvoiceMapper;
import com.amazon.util.InvoiceProductMapper;
import com.amazon.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class InvoiceProductServiceImplTest {

    @Mock
    private InvoiceProductRepository invoiceProductRepository;

    @Mock
    private MapperUtil mapperUtil;

    @Mock
    private InvoiceProductMapper invoiceProductMapper;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private SecurityService securityService;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceProductServiceImpl invoiceProductService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById() throws Exception {
        when(invoiceProductRepository.findById(anyLong())).thenReturn(java.util.Optional.of(new InvoiceProduct()));
        when(mapperUtil.convert(any(), any())).thenReturn(new InvoiceProductDto());
        InvoiceProductDto result = invoiceProductService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void listAllByInvoiceId() {
        InvoiceProduct invoiceProduct = new InvoiceProduct();
        when(invoiceProductRepository.retrieveAllByInvoice_IdAndIsDeletedFalse(anyLong())).thenReturn(Collections.singletonList(invoiceProduct));

        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setQuantity(10); // Ensure quantity is set
        invoiceProductDto.setPrice(BigDecimal.valueOf(100)); // Ensure price is set
        invoiceProductDto.setTax(5); // Ensure tax is set
        when(mapperUtil.convert(any(), any())).thenReturn(invoiceProductDto);

        List<InvoiceProductDto> result = invoiceProductService.listAllByInvoiceId(1L);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getQuantity());
        assertNotNull(result.get(0).getPrice());
        assertNotNull(result.get(0).getTax());
    }
    @Test
    void calculateInvoiceProductTotal() {
        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setPrice(BigDecimal.TEN);
        invoiceProductDto.setQuantity(1);
        invoiceProductDto.setTax(10);
        BigDecimal result = invoiceProductService.calculateInvoiceProductTotal(invoiceProductDto);
        assertEquals(new BigDecimal("11.00"), result);
    }

    @Test
    void setInvoiceTotal() {
        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setPrice(BigDecimal.TEN);
        invoiceProductDto.setQuantity(1);
        invoiceProductDto.setTax(10);
        InvoiceProductDto result = invoiceProductService.setInvoiceTotal(invoiceProductDto);
        assertNotNull(result.getTotal());
    }

    @Test
    void calculateAndSetInvoiceTotals() {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(1L);
        when(securityService.getCurrentUserCompany()).thenReturn(companyDto);

        Invoice invoice = new Invoice();
        when(invoiceRepository.findByIdAndCompanyIdAndInvoiceStatus(anyLong(), anyLong(), any())).thenReturn(invoice);

        InvoiceDto invoiceDto = new InvoiceDto();
        when(invoiceMapper.convertToDto(any(Invoice.class))).thenReturn(invoiceDto);

        InvoiceProduct invoiceProduct = new InvoiceProduct();
        when(invoiceProductRepository.retrieveAllByInvoice_IdAndIsDeletedFalse(anyLong())).thenReturn(Collections.singletonList(invoiceProduct));

        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setQuantity(10);
        invoiceProductDto.setPrice(BigDecimal.valueOf(100));
        invoiceProductDto.setTax(5); // Ensure tax is set
        when(mapperUtil.convert(any(), any())).thenReturn(invoiceProductDto);

        InvoiceDto result = invoiceProductService.calculateAndSetInvoiceTotals(1L);
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000).setScale(2), result.getPrice());
        assertEquals(BigDecimal.valueOf(50).setScale(2), result.getTax());
        assertEquals(BigDecimal.valueOf(1050).setScale(2), result.getTotal());
    }

    @Test
    void calculateTotal() {
        BigDecimal result = invoiceProductService.calculateTotal(BigDecimal.TEN, 2, 10);
        assertEquals(new BigDecimal("22.00"), result.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void findApprovedInvoices() {
        when(invoiceProductRepository.findApprovedInvoices(any())).thenReturn(Collections.singletonList(new InvoiceProduct()));
        List<InvoiceProduct> result = invoiceProductService.findApprovedInvoices();
        assertFalse(result.isEmpty());
    }

    @Test
    void getActiveProductsByInvoiceId() {
        when(invoiceProductRepository.findActiveProductsByInvoiceId(anyLong())).thenReturn(Collections.singletonList(new InvoiceProduct()));
        when(invoiceProductMapper.convertToDto(any())).thenReturn(new InvoiceProductDto());
        List<InvoiceProductDto> result = invoiceProductService.getActiveProductsByInvoiceId(1L);
        assertFalse(result.isEmpty());
    }

    @Test
    void addInvoiceProduct() {
        Invoice invoice = new Invoice();
        Product product = new Product();
        product.setId(1L);
        product.setQuantityInStock(10);

        // Mock the repository methods to return the objects we want
        when(invoiceRepository.findById(anyLong())).thenReturn(java.util.Optional.of(invoice));
        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));

        // Mock the mapper to convert the DTO to the entity
        when(mapperUtil.convert(any(InvoiceProductDto.class), any(InvoiceProduct.class)))
                .thenAnswer(invocation -> {
                    InvoiceProductDto dto = invocation.getArgument(0);
                    InvoiceProduct entity = invocation.getArgument(1);
                    entity.setInvoice(invoice);
                    entity.setProduct(product);
                    entity.setQuantity(dto.getQuantity());
                    entity.setPrice(dto.getPrice());
                    entity.setTax(dto.getTax());
                    return entity;
                });

        // Verify that the InvoiceProduct entity has the correct properties set
        doAnswer(invocation -> {
            InvoiceProduct invoiceProduct = invocation.getArgument(0);
            assertEquals(invoice, invoiceProduct.getInvoice());
            return null;
        }).when(invoiceProductRepository).save(any(InvoiceProduct.class));

        // Verify that the Product entity has the correct quantity in stock
        doAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertEquals(9, p.getQuantityInStock());
            return null;
        }).when(productRepository).save(any(Product.class));

        // Prepare the DTO to pass to the service method
        InvoiceProductDto newInvoiceProductDto = new InvoiceProductDto();
        ProductDto productDto = new ProductDto();
        productDto.setId(1L);
        newInvoiceProductDto.setProduct(productDto);
        newInvoiceProductDto.setQuantity(1);
        newInvoiceProductDto.setPrice(BigDecimal.valueOf(100));
        newInvoiceProductDto.setTax(10);

        // Call the service method and assert no exceptions are thrown
        assertDoesNotThrow(() -> invoiceProductService.addInvoiceProduct(1L, newInvoiceProductDto));
    }

    @Test
    void findAllByInvoiceIdAndIsDeleted() throws NullPointerException{
        InvoiceProduct invoiceProduct = new InvoiceProduct();
        when(invoiceProductRepository.findAllByInvoiceIdAndIsDeleted(anyLong(), anyBoolean())).thenReturn(Collections.singletonList(invoiceProduct));

        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setQuantity(10);
        invoiceProductDto.setPrice(BigDecimal.valueOf(100));
        invoiceProductDto.setTax(5); // Ensure tax is set
        when(mapperUtil.convert(any(), any())).thenReturn(invoiceProductDto);

        List<InvoiceProductDto> result = invoiceProductService.findAllByInvoiceIdAndIsDeleted(1L, false);
        assertFalse(result.isEmpty());
    }

    @Test
    void testCalculateTotal() {
        BigDecimal result = invoiceProductService.calculateTotal(BigDecimal.TEN, 2, BigDecimal.TEN);
        assertEquals(new BigDecimal("22.00"), result.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void saveInvoiceProduct() {
        // Prepare test data
        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setQuantity(10); // Ensure quantity is set
        invoiceProductDto.setPrice(BigDecimal.valueOf(100)); // Ensure price is set
        ProductDto productDto = new ProductDto();
        productDto.setId(1L); // Ensure product ID is set
        invoiceProductDto.setProduct(productDto);
        invoiceProductDto.setTax(10); // Ensure tax is set

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(1L); // Ensure invoice ID is set

        // Mock dependencies
        when(invoiceService.findById(anyLong())).thenReturn(invoiceDto);
        when(mapperUtil.convert(any(), any())).thenAnswer(invocation -> {
            Object source = invocation.getArguments()[0];
            Object target = invocation.getArguments()[1];

            if (source instanceof InvoiceProductDto && target instanceof InvoiceProduct) {
                InvoiceProductDto src = (InvoiceProductDto) source;
                InvoiceProduct tgt = (InvoiceProduct) target;

                tgt.setQuantity(src.getQuantity());
                tgt.setPrice(src.getPrice());
                tgt.setTax(src.getTax());
                // Set other properties as needed
                Product product = new Product();
                product.setId(src.getProduct().getId());
                tgt.setProduct(product);
            } else if (source instanceof InvoiceDto && target instanceof Invoice) {
                InvoiceDto src = (InvoiceDto) source;
                Invoice tgt = (Invoice) target;

                tgt.setId(src.getId());
                // Set other properties as needed
            }

            return target;
        });
        when(invoiceProductRepository.save(any())).thenReturn(new InvoiceProduct());

        // Test method execution
        assertDoesNotThrow(() -> invoiceProductService.saveInvoiceProduct(invoiceProductDto, 1L));

        // Verify the save method was called
        verify(invoiceProductRepository, times(1)).save(any(InvoiceProduct.class));
    }
}