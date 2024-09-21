package com.amazon.service;

import com.amazon.dto.ClientVendorDto;
import com.amazon.exception.ClientVendorNotFoundException;

import java.util.List;

public interface ClientVendorService {
    List<ClientVendorDto> listAllClientVendors() throws ClientVendorNotFoundException;

    List<ClientVendorDto> listAllByCompanyTitle() throws ClientVendorNotFoundException;

    ClientVendorDto findById(Long id) throws ClientVendorNotFoundException;

    void save(ClientVendorDto clientVendorDto) throws ClientVendorNotFoundException;

    void update(ClientVendorDto clientVendorDto) throws ClientVendorNotFoundException;

    List<ClientVendorDto> listClientVendorsByCompany() throws ClientVendorNotFoundException;

    boolean hasInvoice(Long id);

    void delete(Long id);


}
