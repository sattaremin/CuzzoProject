package com.amazon.service.impl;

import com.amazon.dto.ClientVendorDto;
import com.amazon.entity.ClientVendor;
import com.amazon.entity.Invoice;
import com.amazon.exception.ClientVendorNotFoundException;
import com.amazon.repository.ClientVendorRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.SecurityService;
import com.amazon.util.ClientVendorMapper;
import com.amazon.util.MapperUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientVendorServiceImplTest {
    @InjectMocks
    private ClientVendorServiceImpl clientVendorService;
    @Mock
    private ClientVendorRepository clientVendorRepository;
    @Mock
    private MapperUtil mapperUtil;
    @Mock
    private ClientVendorMapper clientVendorMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    void findById() {

        ClientVendor clientVendor = new ClientVendor();
        ClientVendorDto clientVendorDto = new ClientVendorDto();

        when(clientVendorRepository.findById(anyLong())).thenReturn(Optional.of(clientVendor));
        when(clientVendorMapper.convertToDto(clientVendor)).thenReturn(clientVendorDto);

        ClientVendorDto actual = clientVendorService.findById(anyLong());

        assertEquals(clientVendorDto, actual);

    }

    @Test
    void findById_throw_ClientVendorNotFoundException() {

        when(clientVendorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ClientVendorNotFoundException.class, () -> clientVendorService.findById(anyLong()));

    }

    @Test
    void hasInvoice_true() {

        when(invoiceRepository.findByClientVendorId(anyLong())).thenReturn(List.of(new Invoice(), new Invoice()));

        assertTrue(clientVendorService.hasInvoice(anyLong()));
    }

    @Test
    void hasInvoice_false() {

        when(invoiceRepository.findByClientVendorId(anyLong())).thenReturn(List.of());

        assertFalse(clientVendorService.hasInvoice(anyLong()));
    }
}