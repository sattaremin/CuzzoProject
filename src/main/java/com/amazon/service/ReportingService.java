package com.amazon.service;

import com.amazon.dto.InvoiceProductDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ReportingService {

    List<InvoiceProductDto>getStockDetails();

    Map<String, BigDecimal> getMonthlyProfitLoss();
}
