package com.amazon.integarationTesting;


import com.amazon.converter.ProductDtoConverter;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.dto.ProductDto;
import com.amazon.entity.Invoice;
import com.amazon.entity.InvoiceProduct;
import com.amazon.entity.Product;
import com.amazon.enums.InvoiceStatus;
import com.amazon.exception.InsufficientStockException;
import com.amazon.exception.InvoiceProductNotFoundException;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.CompanyService;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.InvoiceService;
import com.amazon.service.ProductService;
import com.amazon.util.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@WithMockUser(username = "manager@greentech.com", password = "Abc1", roles = "MANAGER")
public class InvoiceProductServiceImpl_IntegrationTesting {


    @Autowired
    InvoiceProductRepository invoiceProductRepository;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    ProductService productService;

    @Autowired
    CompanyService companyService;

    @Autowired
    InvoiceProductService invoiceProductService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDtoConverter productDtoConverter;
    @Autowired
    private ProductMapper productMapper;

    @BeforeEach
    void setUp(){



    }


    @Test
    void should_find_by_id() {
        // when
        InvoiceProductDto actualResult = invoiceProductService.findById(1L);
        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getPrice()).isCloseTo(BigDecimal.valueOf(250.00), withinPercentage(5));
    }

    @Test
    void findById_shouldThrowException() {
        // when
        Throwable throwable = catchThrowable(() -> invoiceProductService.findById(0L));
        // then
        assertThat(throwable).isInstanceOf(InvoiceProductNotFoundException.class);
        // or
        assertInstanceOf(InvoiceProductNotFoundException.class, throwable);

    }

    @Test
    void findAllByInvoiceIdAndCalculateTotalPrice_shouldCalculateTotalPrice() {
        // when

        List<InvoiceProductDto> actualList = invoiceProductService.findAllByInvoiceIdAndIsDeleted(1L, false);

        // then
        assertThat(actualList.get(0).getTotal()).isCloseTo(BigDecimal.valueOf(1275), withinPercentage(10L));
    }

    @Test
    public void whenFindApprovedInvoices_thenReturnApprovedInvoices() {
        List<InvoiceProduct> approvedInvoices = invoiceProductService.findApprovedInvoices();
        assertThat(approvedInvoices).isNotNull();
        assertThat(approvedInvoices).allMatch(invoiceProduct -> invoiceProduct.getInvoice().getInvoiceStatus().equals(InvoiceStatus.APPROVED));
    }

    @Test
    public void whenGetActiveProductsByInvoiceId_thenReturnActiveInvoiceProducts() {
        List<InvoiceProductDto> activeProducts = invoiceProductService.getActiveProductsByInvoiceId(1L);

        assertThat(activeProducts).isNotNull();
    }

    @Test
    public void whenAddInvoiceProductWithInsufficientStock_thenThrowException() {
        Invoice invoice = invoiceRepository.findAll().get(0);
        Product product = productRepository.findAll().get(0);

        ProductDto productDto = productMapper.convertToDto(product);

        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();
        invoiceProductDto.setProduct(productDto);
        invoiceProductDto.setQuantity(200); // Exceeds available stock
        invoiceProductDto.setPrice(new BigDecimal("10.00"));
        invoiceProductDto.setTax(20);

        assertThrows(InsufficientStockException.class, () -> {
            invoiceProductService.addInvoiceProduct(invoice.getId(), invoiceProductDto);
        });
    }

























}
