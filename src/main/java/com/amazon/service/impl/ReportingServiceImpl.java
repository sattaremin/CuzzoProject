package com.amazon.service.impl;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.entity.Invoice;
import com.amazon.enums.InvoiceStatus;
import com.amazon.repository.InvoiceRepository;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.ReportingService;
import com.amazon.service.SecurityService;
import com.amazon.util.CompanyMapper;
import com.amazon.util.MapperUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final InvoiceProductService invoiceProductService;
    private final MapperUtil mapperUtil;
    private final InvoiceRepository invoiceRepository;


    private final SecurityService securityService;
    private final CompanyMapper companyMapper;


    public ReportingServiceImpl(InvoiceProductService invoiceProductService, MapperUtil mapperUtil, InvoiceRepository invoiceRepository, SecurityService securityService, CompanyMapper companyMapper) {
        this.invoiceProductService = invoiceProductService;
        this.mapperUtil = mapperUtil;

        this.invoiceRepository = invoiceRepository;
        this.securityService = securityService;
        this.companyMapper = companyMapper;
    }


    @Override
    public List<InvoiceProductDto> getStockDetails() {
        return invoiceProductService.findApprovedInvoices().stream().map(invoice -> mapperUtil.convert(invoice, new InvoiceProductDto())).collect(Collectors.toList());
    }

@Override
    public Map<String, BigDecimal> getMonthlyProfitLoss() {
        CompanyDto currentUserCompany = securityService.getCurrentUserCompany();
        if (currentUserCompany == null) {
            return new LinkedHashMap<>();
        }

        List<Invoice> approvedInvoices = invoiceRepository.findAllByInvoiceStatusAndCompany(
                InvoiceStatus.APPROVED,
                companyMapper.convertToEntity(currentUserCompany)
        );

        return approvedInvoices.stream()
                .sorted((i1, i2) -> i2.getDate().compareTo(i1.getDate()))
                .flatMap(invoice -> invoice.getInvoiceProducts().stream()
                        .map(product -> new AbstractMap.SimpleEntry<>(
                                invoice.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                                product.getProfitLoss())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Map.Entry::getValue,
                                BigDecimal::add
                        )
                ));

    }
}

