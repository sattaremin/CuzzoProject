package com.amazon.service;

import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.entity.InvoiceProduct;
import com.amazon.enums.InvoiceType;
import com.amazon.exception.InvoiceNotFoundException;

import java.util.List;

public interface InvoiceService {

    InvoiceDto findById(Long id) throws InvoiceNotFoundException;

    List<InvoiceDto> listAllInvoices(InvoiceType invoiceType);

    List<InvoiceDto> listAllPurchaseInvoices();

    List<InvoiceDto> listAllSalesInvoices();

    void save(InvoiceDto invoiceDto, InvoiceType invoiceType);


    void approvePurchaseInvoice(Long id );


    String newInvoiceNo(InvoiceType invoiceType);

    void removeInvoiceById(Long id);


    void approveSalesInvoice(Long invoiceId);

    void updateInvoice(InvoiceDto invoiceDto);

    void addInvoiceProduct(Long invoiceId, InvoiceProductDto newInvoiceProductDto) throws InvoiceNotFoundException;

    void removeInvoiceProduct(Long invoiceId, Long invoiceProductId);

    List<InvoiceProductDto> findAllInvoiceProductsByInvoiceId(Long invoiceId);

    void calculateTotalWithTax(InvoiceProduct invoiceProduct);


}
