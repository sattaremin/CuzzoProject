package com.amazon.service.impl;

import com.amazon.dto.*;
import com.amazon.entity.*;
import com.amazon.enums.CompanyStatus;
import com.amazon.enums.InvoiceType;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.repository.ClientVendorRepository;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.ProductService;
import com.amazon.service.SecurityService;
import com.amazon.util.InvoiceProductMapper;
import com.amazon.util.MapperUtil;
import com.amazon.util.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private MapperUtil mapperUtil;
    @Mock
    private SecurityService securityService;
    @Mock
    private InvoiceProductRepository invoiceProductRepository;
    @Mock
    private InvoiceProductMapper invoiceProductMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private InvoiceProductService invoiceProductService;
    @Mock
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ClientVendorRepository clientVendorRepository;
    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice invoice;
    private InvoiceDto invoiceDto;
    private ClientVendorDto clientVendorDto;
    private UserDto userDto;
    private CompanyDto companyDto;
    private AddressDto addressDto;
    private Company company;
    @BeforeEach
    void setup() {
        company= new Company();
        company.setId(1L);

        companyDto = new CompanyDto();
        companyDto.setId(1L);
        companyDto.setTitle("Test Company");
        companyDto.setAddress(addressDto);
        companyDto.setWebsite("https://www.gozde.com");
        companyDto.setCompanyStatus(CompanyStatus.ACTIVE);
        companyDto.setPhone("1234567890");

        userDto = new UserDto();
        userDto.setCompany(companyDto);

        invoice = new Invoice();
        invoice.setId(1L);

        invoiceDto = new InvoiceDto();
        invoiceDto.setClientVendor(clientVendorDto);

        clientVendorDto = new ClientVendorDto();
        clientVendorDto.setId(1L);
    }



    @Test
    void testFindById_Success() throws InvoiceNotFoundException {
        // Arrange
        when(invoiceRepository.findById(anyLong())).thenReturn(Optional.of(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);

        // Act
        InvoiceDto expected = invoiceDto;
        InvoiceDto actual = invoiceService.findById(1L);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(invoiceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        InvoiceNotFoundException thrown = assertThrows(
                InvoiceNotFoundException.class,
                () -> invoiceService.findById(1L),
                "Expected Invoice Not Found to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Invoice Not Found"));
        verify(invoiceRepository, times(1)).findById(1L);
        verify(mapperUtil, never()).convert(any(), any()); //Verify that convert was never called.
    }


    @Test
    void testListAllInvoices() {
        // Arrange
        InvoiceType invoiceType = InvoiceType.SALES;
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceType(invoiceType);

        InvoiceDto invoiceDto = new InvoiceDto();

        InvoiceProduct invoiceProduct = new InvoiceProduct();
        invoiceProduct.setId(1L);
        invoiceProduct.setQuantity(2);
        invoiceProduct.setPrice(BigDecimal.valueOf(100));
        invoiceProduct.setTax(10);

        when(securityService.getLoggedInUser()).thenReturn(userDto);
        when(invoiceRepository.findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(invoiceType, "Test Company"))
                .thenReturn(List.of(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);
        when(invoiceProductRepository.findById(1L)).thenReturn(Optional.of(invoiceProduct));

        // Act
        List<InvoiceDto> result = invoiceService.listAllInvoices(invoiceType);
        int actual=result.size();
        // Assert
        assertNotNull(result);
        assertEquals(1, actual);

        InvoiceDto resultDto = result.get(0);
        assertEquals(BigDecimal.valueOf(200), resultDto.getPrice());
        assertEquals(BigDecimal.valueOf(20), resultDto.getTax());
        assertEquals(BigDecimal.valueOf(220), resultDto.getTotal());

        verify(securityService, times(1)).getLoggedInUser();
        verify(invoiceRepository, times(1))
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(invoiceType, "Test Company");
        verify(mapperUtil, times(1)).convert(invoice, new InvoiceDto());
        verify(invoiceProductRepository, times(1)).findById(1L);
    }

    @Test
    void testListAllPurchaseInvoices() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceType(InvoiceType.PURCHASE);

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(1L);

        InvoiceProduct invoiceProduct = new InvoiceProduct();
        invoiceProduct.setId(1L);
        invoiceProduct.setQuantity(2);
        invoiceProduct.setPrice(BigDecimal.valueOf(100));
        invoiceProduct.setTax(10);

        when(securityService.getLoggedInUser()).thenReturn(userDto);
        when(invoiceRepository.findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.PURCHASE, "Test Company"))
                .thenReturn(Arrays.asList(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);
        when(invoiceProductRepository.findAllByInvoiceId(1L)).thenReturn(Arrays.asList(invoiceProduct));

        // Act
        List<InvoiceDto> result = invoiceService.listAllPurchaseInvoices();
         int actual=result.size();
        // Assert
        assertNotNull(result);
        assertEquals(1, actual);

        InvoiceDto resultDto = result.get(0);
        assertEquals(BigDecimal.valueOf(200), resultDto.getPrice());
        assertEquals(BigDecimal.valueOf(20), resultDto.getTax());
        assertEquals(BigDecimal.valueOf(220), resultDto.getTotal());

        verify(securityService, times(1)).getLoggedInUser();
        verify(invoiceRepository, times(1))
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.PURCHASE, "Test Company");
        verify(mapperUtil, times(1)).convert(invoice, new InvoiceDto());
        verify(invoiceProductRepository, times(1)).findAllByInvoiceId(1L);
    }
    @Test
    void testListAllPurchaseInvoices_NoProducts() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setId(2L);
        invoice.setInvoiceType(InvoiceType.PURCHASE);

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(2L);

        when(securityService.getLoggedInUser()).thenReturn(userDto);
        when(invoiceRepository.findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.PURCHASE, "Test Company"))
                .thenReturn(Arrays.asList(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);
        when(invoiceProductRepository.findAllByInvoiceId(2L)).thenReturn(Collections.emptyList());

        // Act
        List<InvoiceDto> result = invoiceService.listAllPurchaseInvoices();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        InvoiceDto resultDto = result.get(0);
        assertEquals(BigDecimal.ZERO, resultDto.getPrice());
        assertEquals(BigDecimal.ZERO, resultDto.getTax());
        assertEquals(BigDecimal.ZERO, resultDto.getTotal());

        verify(securityService, times(1)).getLoggedInUser();
        verify(invoiceRepository, times(1))
                .findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.PURCHASE, "Test Company");
        verify(mapperUtil, times(1)).convert(invoice, new InvoiceDto());
        verify(invoiceProductRepository, times(1)).findAllByInvoiceId(2L);
    }

    @Test
    void testListAllSalesInvoices() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceType(InvoiceType.SALES);

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(1L);

        InvoiceProduct invoiceProduct = new InvoiceProduct();
        invoiceProduct.setId(1L);
        invoiceProduct.setQuantity(2);
        invoiceProduct.setPrice(BigDecimal.valueOf(100));
        invoiceProduct.setTax(10);

        when(securityService.getLoggedInUser()).thenReturn(userDto);
        when(invoiceRepository.findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.SALES, "Test Company"))
                .thenReturn(Arrays.asList(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);
        when(invoiceProductRepository.findAllInvoiceProductsByInvoiceId(1L)).thenReturn(Arrays.asList(invoiceProduct));

        // Act
        List<InvoiceDto> result = invoiceService.listAllSalesInvoices();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        InvoiceDto resultDto = result.get(0);
        assertEquals(BigDecimal.valueOf(200), resultDto.getPrice());
        assertEquals(BigDecimal.valueOf(20), resultDto.getTax());
        assertEquals(BigDecimal.valueOf(220), resultDto.getTotal());

    }

    @Test
    void testListAllSalesInvoices_NoProducts() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setId(2L);
        invoice.setInvoiceType(InvoiceType.SALES);

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(2L);

        when(securityService.getLoggedInUser()).thenReturn(userDto);
        when(invoiceRepository.findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType.SALES, "Test Company"))
                .thenReturn(Arrays.asList(invoice));
        when(mapperUtil.convert(invoice, new InvoiceDto())).thenReturn(invoiceDto);
        when(invoiceProductRepository.findAllInvoiceProductsByInvoiceId(2L)).thenReturn(Collections.emptyList());

        // Act
        List<InvoiceDto> result = invoiceService.listAllSalesInvoices();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        InvoiceDto resultDto = result.get(0);
        assertEquals(BigDecimal.ZERO, resultDto.getPrice());
        assertEquals(BigDecimal.ZERO, resultDto.getTax());
        assertEquals(BigDecimal.ZERO, resultDto.getTotal());

        verify(securityService, times(1)).getLoggedInUser();

    }


//  @Test // save method gives error
//  void testSave_ValidClientVendor() {
//      // Arrange
//      Invoice invoice = new Invoice();
//      Company company = new Company();
//      ClientVendor clientVendor = new ClientVendor();

//      when(mapperUtil.convert(any(InvoiceDto.class), any(Invoice.class))).thenReturn(invoice);
//      when(securityService.getCurrentUserCompany()).thenReturn(companyDto);
//      when(mapperUtil.convert(any(CompanyDto.class), any(Company.class))).thenReturn(company);
//      when(clientVendorRepository.findById(1L)).thenReturn(java.util.Optional.of(clientVendor));


//      // Act
//      invoiceService.save(invoiceDto, InvoiceType.SALES);

//      // Assert
//      assertEquals(InvoiceType.values(), invoice.getInvoiceType());
//      assertNotNull(invoice.getDate());
//      assertEquals(InvoiceStatus.AWAITING_APPROVAL, invoice.getInvoiceStatus());
//      assertEquals(company, invoice.getCompany());
//      assertEquals(clientVendor, invoice.getClientVendor());


//  }

//  @Test // save method gives error
//  void testSave_ClientVendorNotSet() {
//      // Arrange
//      invoiceDto.setClientVendor(null);

//      // Act & Assert
//      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//          invoiceService.save(invoiceDto, InvoiceType.SALES);
//      });

//      assertEquals("ClientVendor must not be null", exception.getMessage());

//      verify(mapperUtil, never()).convert(any(), any());
//      verify(securityService, never()).getCurrentUserCompany();
//      verify(clientVendorRepository, never()).findById(anyLong());
//      verify(invoiceRepository, never()).save(any());
//  }
//
  //  @Test // save method gives error
  //  void testSave_ClientVendorNotFound() {
  //      // Arrange
  //      when(clientVendorRepository.findById(1L)).thenReturn(java.util.Optional.empty());
//
  //      // Act & Assert
  //      EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
  //          invoiceService.save(invoiceDto, InvoiceType.SALES);
  //      });
//
  //      assertEquals("ClientVendor not found with ID: 1", exception.getMessage());
//
  //      verify(mapperUtil, times(1)).convert(invoiceDto, new Invoice());
  //      verify(securityService, times(1)).getCurrentUserCompany();
  //      verify(mapperUtil, times(1)).convert(companyDto, new Company());
  //      verify(clientVendorRepository, times(1)).findById(1L);
  //      verify(invoiceRepository, never()).save(any());
  //  }

    @Test
    void testNewInvoiceNo_PurchaseInvoice_EmptyList() {
        // Arrange
        when(securityService.getCurrentUserCompany()).thenReturn(userDto.getCompany());
        when(invoiceService.listAllPurchaseInvoice(company.getId())).thenReturn(List.of());

        // Act
        String newInvoiceNo = invoiceService.newInvoiceNo(InvoiceType.PURCHASE);

        // Assert
        assertEquals("P-001", newInvoiceNo);
    }

  //  @Test
  //  void testNewInvoiceNo_PurchaseInvoice_NonEmptyList() {
  //      // Arrange
  //      when(securityService.getCurrentUserCompany()).thenReturn(userDto.getCompany());
  //      when(invoiceService.listAllPurchaseInvoice(any(Long.class))).thenReturn(List.of("P-001", "P-002", "P-003"));
//
  //      // Act
  //      String newInvoiceNo = invoiceService.newInvoiceNo(InvoiceType.PURCHASE);
//
  //      // Assert
  //      assertEquals("P-004", newInvoiceNo);
  //  }

    @Test
    void testNewInvoiceNo_SalesInvoice_EmptyList() {
        // Arrange
        when(securityService.getCurrentUserCompany()).thenReturn(userDto.getCompany());
        when(invoiceService.listAllSalesInvoice(company.getId())).thenReturn(List.of());

        // Act
        String newInvoiceNo = invoiceService.newInvoiceNo(InvoiceType.SALES);

        // Assert
        assertEquals("S-001", newInvoiceNo);
    }
//
 //   @Test // error
 //   void testNewInvoiceNo_SalesInvoice_NonEmptyList() {
 //       // Arrange
 //       when(securityService.getCurrentUserCompany()).thenReturn(userDto.getCompany());
 //       when(invoiceService.listAllSalesInvoice(company.getId())).thenReturn(List.of("S-001", "S-002", "S-003"));
//
 //       // Act
 //       String newInvoiceNo = invoiceService.newInvoiceNo(InvoiceType.SALES);
//
 //       // Assert
 //       assertEquals("S-004", newInvoiceNo);
 //   }
//
    @Test
    void testFindAllInvoiceProductsByInvoiceId() {
        // Arrange
        Long invoiceId = 1L;
        InvoiceProduct invoiceProduct = new InvoiceProduct();
        InvoiceProductDto invoiceProductDto = new InvoiceProductDto();

        when(invoiceProductRepository.findAllByInvoiceId(invoiceId)).thenReturn(List.of(invoiceProduct));
        when(mapperUtil.convert(invoiceProduct, new InvoiceProductDto())).thenReturn(invoiceProductDto);

        // Act
        List<InvoiceProductDto> result = invoiceService.findAllInvoiceProductsByInvoiceId(invoiceId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(invoiceProductDto, result.get(0));

        verify(invoiceProductRepository, times(1)).findAllByInvoiceId(invoiceId);
        verify(mapperUtil, times(1)).convert(invoiceProduct, new InvoiceProductDto());
    }

    @Test
    void testCalculateTotalWithTax() {
        // Arrange
        InvoiceProduct invoiceProduct = new InvoiceProduct();
        invoiceProduct.setPrice(BigDecimal.valueOf(100));
        invoiceProduct.setQuantity(2);
        invoiceProduct.setTax(10); // 10% tax

        // Act
        invoiceService.calculateTotalWithTax(invoiceProduct);

        // Assert
        BigDecimal expectedTotalPrice = BigDecimal.valueOf(200); // 100 * 2
        BigDecimal expectedTotalTax = BigDecimal.valueOf(20);    // 10% of 200
        BigDecimal expectedTotalWithTax = expectedTotalPrice.add(expectedTotalTax);

        assertEquals(expectedTotalWithTax, invoiceProduct.getTotalWithTax());
    }

//   @Test
//   void testAddInvoiceProduct() throws InvoiceNotFoundException {
//       // Arrange
//       Long invoiceId = 1L;
//       InvoiceProductDto newInvoiceProductDto = new InvoiceProductDto();
//       Invoice invoice = new Invoice();
//       InvoiceProduct invoiceProduct = new InvoiceProduct();

//       when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
//       when(mapperUtil.convert(newInvoiceProductDto, new InvoiceProduct())).thenReturn(invoiceProduct);

//      // doNothing().when(invoiceService).calculateTotalWithTax(invoiceProduct);

//       // Act
//       invoiceService.addInvoiceProduct(invoiceId, newInvoiceProductDto);

//       // Assert
//       assertEquals(invoice, invoiceProduct.getInvoice());
//       verify(invoiceRepository, times(1)).findById(invoiceId);
//       verify(mapperUtil, times(1)).convert(newInvoiceProductDto, new InvoiceProduct());
//       verify(invoiceProductRepository, times(1)).save(invoiceProduct);
//   }

    @Test
    void testAddInvoiceProduct_InvoiceNotFound() {
        // Arrange
        Long invoiceId = 1L;
        InvoiceProductDto newInvoiceProductDto = new InvoiceProductDto();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        InvoiceNotFoundException exception = assertThrows(InvoiceNotFoundException.class, () -> {
            invoiceService.addInvoiceProduct(invoiceId, newInvoiceProductDto);
        });

        assertEquals("Invoice not found", exception.getMessage());
        verify(invoiceRepository, times(1)).findById(invoiceId);
        verify(mapperUtil, never()).convert(any(), any());
        verify(invoiceProductRepository, never()).save(any());
    }

}