package com.amazon.converter;

import com.amazon.dto.InvoiceProductDto;
import com.amazon.exception.InvoiceProductNotFoundException;
import com.amazon.service.InvoiceProductService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InvoiceProductDTOConverter implements Converter<String, InvoiceProductDto> {

    private final InvoiceProductService invoiceProductService;

    public InvoiceProductDTOConverter(InvoiceProductService invoiceProductService) {
        this.invoiceProductService = invoiceProductService;
    }

    @Override
    public InvoiceProductDto convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return this.invoiceProductService.findById(Long.valueOf(source));
        } catch (InvoiceProductNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
