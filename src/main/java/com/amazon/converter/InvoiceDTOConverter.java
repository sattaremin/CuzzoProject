package com.amazon.converter;

import com.amazon.dto.InvoiceDto;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.service.InvoiceService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDTOConverter implements Converter<String, InvoiceDto> {

    private final InvoiceService invoiceService;

    public InvoiceDTOConverter(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public InvoiceDto convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return this.invoiceService.findById(Long.valueOf(source));
        } catch (InvoiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
