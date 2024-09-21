package com.amazon.service;

import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.entity.InvoiceProduct;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.exception.InvoiceProductNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceProductService {

    InvoiceProductDto findById(Long id) throws InvoiceProductNotFoundException;

    void saveInvoiceProduct(InvoiceProductDto invoiceProductDto,Long invoiceId);

    List<InvoiceProductDto> listAllByInvoiceId(Long invoiceId);

    BigDecimal calculateInvoiceProductTotal(InvoiceProductDto invoiceProductDto);

    BigDecimal calculateTotal(BigDecimal price, Integer quantity, BigDecimal tax);

    BigDecimal calculateTotal(BigDecimal price, Integer quantity, Integer tax);

    List<InvoiceProduct> findApprovedInvoices();

    List<InvoiceProductDto> getActiveProductsByInvoiceId(Long invoiceId);

    void addInvoiceProduct(Long invoiceId, InvoiceProductDto newInvoiceProductDto) throws InvoiceNotFoundException;

    List<InvoiceProductDto> findAllByInvoiceIdAndIsDeleted(Long id, boolean isDeleted);

    InvoiceProductDto setInvoiceTotal(InvoiceProductDto invoiceProductDto);

    InvoiceDto calculateAndSetInvoiceTotals(Long invoiceId);
}