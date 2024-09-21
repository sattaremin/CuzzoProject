package com.amazon.service.impl;

import com.amazon.dto.InvoiceDto;
import com.amazon.entity.Invoice;
import com.amazon.enums.InvoiceStatus;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.repository.InvoiceRepository;
import com.amazon.service.DashBoardService;
import com.amazon.service.InvoiceProductService;
import com.amazon.service.InvoiceService;
import com.amazon.util.InvoiceMapper;
import com.amazon.util.MapperUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.amazon.entity.InvoiceProduct;
import com.amazon.enums.InvoiceType;
import com.amazon.service.CompanyService;

import java.util.*;


@Service
public class DashboardServiceImpl implements DashBoardService {

    private final InvoiceRepository invoiceRepository;
    private final MapperUtil mapperUtil;
    private final InvoiceProductRepository invoiceProductRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceService invoiceService;
    private final InvoiceProductService invoiceProductService;
    private final CompanyService companyService;


    public DashboardServiceImpl(InvoiceRepository invoiceRepository, MapperUtil mapperUtil, InvoiceProductRepository invoiceProductRepository, InvoiceMapper invoiceMapper, InvoiceService invoiceService, InvoiceProductService invoiceProductService, CompanyService companyService) {

  

        this.invoiceRepository = invoiceRepository;
        this.mapperUtil = mapperUtil;
        this.invoiceProductRepository = invoiceProductRepository;
        this.invoiceMapper = invoiceMapper;
        this.invoiceService = invoiceService;
        this.invoiceProductService = invoiceProductService;
        this.companyService = companyService;
    }


    @Override
    public List<InvoiceDto> getLastThreeApprovedInvoices(InvoiceStatus invoiceStatus) {
        List<Invoice> invoiceList = invoiceRepository.findTop3ByInvoiceStatusOrderByDateDesc(invoiceStatus);
        return invoiceList.stream()
                .map(invoice -> {
                    InvoiceDto invoiceDto = mapperUtil.convert(invoice, new InvoiceDto());

                    BigDecimal[] totalPriceAndTax = calculateTotalPriceAndTaxByInvoiceId(invoiceDto.getId());
                    //BigDecimal[] is a array for price and tax => [0] value is total price
                    //                                          => [1] value is total tax
                    invoiceDto.setPrice( totalPriceAndTax[0]);
                    invoiceDto.setTax( totalPriceAndTax[1]);
                    invoiceDto.setTotal( totalPriceAndTax[0].add( totalPriceAndTax[1]));

                    return invoiceDto;
                })
                .collect(Collectors.toList());
    }


    public BigDecimal[] calculateTotalPriceAndTaxByInvoiceId(Long id) {
        return invoiceProductService.listAllByInvoiceId(id)
                .stream()
                .reduce(new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO},
                        (acc, p) -> {
                            BigDecimal price = p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
                            BigDecimal tax = price.multiply(BigDecimal.valueOf(p.getTax() / 100.0));
                            acc[0] = acc[0].add(price).setScale(2); // Toplam fiyat -   Total value
                            acc[1] = acc[1].add(tax).setScale(2);   // Toplam vergi -   Total tax
                            return acc;
                        },
                        (acc1, acc2) -> {
                            acc1[0] = acc1[0].add(acc2[0]);
                            acc1[1] = acc1[1].add(acc2[1]);
                            return acc1;
                        });
    }

    public BigDecimal calculateTotalCost() {
        String companyTitle = companyService.getCompanyDtoByLoggedInUser().getTitle();
        return invoiceProductRepository.findAll()
                .stream()
                .filter(i ->
                        i.getInvoice().getCompany().getTitle().equals(companyTitle) &&
                                i.getInvoice().getInvoiceType().equals(InvoiceType.PURCHASE) &&
                                i.getInvoice().getInvoiceStatus().equals(InvoiceStatus.APPROVED)
                )
                .map(i -> {
                    BigDecimal totalPrice = i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                    BigDecimal totalTax = totalPrice.multiply(BigDecimal.valueOf(i.getTax()).divide(BigDecimal.valueOf(100)));
                    return totalPrice.add(totalTax);
                })
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotalSales() {
        String companyTitle = companyService.getCompanyDtoByLoggedInUser().getTitle();
        return invoiceProductRepository.findAll()
                .stream()
                .filter(i ->
                        i.getInvoice().getCompany().getTitle().equals(companyTitle) &&
                                i.getInvoice().getInvoiceType().equals(InvoiceType.SALES) &&
                                i.getInvoice().getInvoiceStatus().equals(InvoiceStatus.APPROVED)
                )
                .map(i -> {
                    BigDecimal totalPrice = i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                    BigDecimal totalTax = totalPrice.multiply(BigDecimal.valueOf(i.getTax()).divide(BigDecimal.valueOf(100)));
                    return totalPrice.add(totalTax);
                })
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }


    public BigDecimal calculateProfitLoss() {
        String companyTitle = companyService.getCompanyDtoByLoggedInUser().getTitle();
        return invoiceProductRepository.findAll()
                .stream()
                .filter(i ->
                        i.getInvoice().getCompany().getTitle().equals(companyTitle) &&
                                i.getInvoice().getInvoiceType().equals(InvoiceType.SALES) &&
                                i.getInvoice().getInvoiceStatus().equals(InvoiceStatus.APPROVED)
                )
                .map(InvoiceProduct::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getSummaryNumbers() {
        Map<String, BigDecimal> summaryNumbers = new HashMap<>();
        summaryNumbers.put("totalCost", calculateTotalCost());
        summaryNumbers.put("totalSales", calculateTotalSales());
        summaryNumbers.put("profitLoss", calculateProfitLoss());

        return summaryNumbers;


    }

}


