package com.amazon.service.impl;

import com.amazon.dto.ClientVendorDto;
import com.amazon.entity.ClientVendor;
import com.amazon.entity.Company;
import com.amazon.entity.Invoice;
import com.amazon.entity.User;
import com.amazon.exception.ClientVendorNotFoundException;
import com.amazon.repository.ClientVendorRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.ClientVendorService;
import com.amazon.service.SecurityService;
import com.amazon.util.ClientVendorMapper;
import com.amazon.util.MapperUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientVendorServiceImpl implements ClientVendorService {

    private final ClientVendorRepository clientVendorRepository;
    private final MapperUtil mapperUtil;
    private final ClientVendorMapper clientVendorMapper;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final InvoiceRepository invoiceRepository;


    public ClientVendorServiceImpl(ClientVendorRepository clientVendorRepository,
                                   MapperUtil mapperUtil,
                                   ClientVendorMapper clientVendorMapper,
                                   UserRepository userRepository,
                                   SecurityService securityService,
                                   InvoiceRepository invoiceRepository) {

        this.clientVendorRepository = clientVendorRepository;
        this.mapperUtil = mapperUtil;
        this.clientVendorMapper = clientVendorMapper;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<ClientVendorDto> listAllClientVendors() {

        return clientVendorRepository.findClientVendorsByUserId(securityService.getLoggedInUser().getId()).stream()
                .filter(clientVendor -> !clientVendor.getIsDeleted())
                .map(clientVendor -> {
                    ClientVendorDto dto = mapperUtil.convert(clientVendor, new ClientVendorDto());
                    dto.setHasInvoice(hasInvoice(clientVendor.getId()));
                    return dto;
                })
                .collect(Collectors.toList());


    }

    @Override
    public List<ClientVendorDto> listAllByCompanyTitle() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User byUsername = userRepository.findByUsername(username);
        List<ClientVendor> allByCompanyTitle = clientVendorRepository.findAllByCompanyTitleOrderByClientVendorName(byUsername.getCompany().getTitle());
        return allByCompanyTitle.stream().map(p -> mapperUtil.convert(p, new ClientVendorDto())).collect(Collectors.toList());

    }

    @Override
    public ClientVendorDto findById(Long id) {

        return clientVendorMapper.convertToDto(clientVendorRepository.findById(id)
                .orElseThrow(() -> new ClientVendorNotFoundException("ClientVendor not found")));

    }

    @Override
    public void save(ClientVendorDto clientVendorDto) {

        ClientVendor converted = clientVendorMapper.convertToEntity(clientVendorDto);
        converted.setCompany(mapperUtil.convert(securityService.getLoggedInUser().getCompany(), new Company()));
        clientVendorRepository.save(converted);
    }

    @Override
    public void update(ClientVendorDto clientVendorDto) {

        if (clientVendorDto == null || clientVendorDto.getId() == null) {
            throw new ClientVendorNotFoundException("ClientVendorDto or ClientVendor ID cannot found");
        }
        ClientVendor clientVendor = clientVendorRepository.getReferenceById(clientVendorDto.getId());
        if (clientVendor == null) {
            throw new ClientVendorNotFoundException("Client/Vendor cannot be found with ID " + clientVendor.getId());
        }

        ClientVendor convertedClientVendor = clientVendorMapper.convertToEntity(clientVendorDto);
        convertedClientVendor.setId(clientVendor.getId());
        clientVendorRepository.save(convertedClientVendor);
    }

    @Override
    public List<ClientVendorDto> listClientVendorsByCompany() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User byUsername = userRepository.findByUsername(username);

        return clientVendorRepository.findAllByCompanyTitle(byUsername.getCompany().getTitle()).stream()
                .map(clientVendor -> mapperUtil.convert(clientVendor, new ClientVendorDto())).collect(Collectors.toList());

    }

    @Override
    public boolean hasInvoice(Long id) {

        List<Invoice> invoices = invoiceRepository.findByClientVendorId(id);
        return !invoices.isEmpty();

    }

    @Override
    public void delete(Long id) {

        ClientVendor clientVendor = clientVendorRepository.findById(id)
                .orElseThrow(() -> new ClientVendorNotFoundException("Client/Vendor not found"));
        if (hasInvoice(id)) {
            throw new IllegalStateException("Can not be deleted! This client / vendor has invoice(s).");
        }

        clientVendor.setIsDeleted(true);
        clientVendor.getInvoices().forEach(invoice -> {
            invoice.setIsDeleted(true);
            invoice.getInvoiceProducts().forEach(invoiceProduct -> invoiceProduct.setIsDeleted(true));
        });

        clientVendorRepository.save(clientVendor);
    }

}




