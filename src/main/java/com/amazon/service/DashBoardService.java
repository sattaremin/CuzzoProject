package com.amazon.service;

import com.amazon.dto.InvoiceDto;
import com.amazon.enums.InvoiceStatus;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

public interface DashBoardService {

    BigDecimal calculateProfitLoss();
    BigDecimal calculateTotalSales();
    BigDecimal calculateTotalCost();
    Map<String, BigDecimal> getSummaryNumbers();
  
      List<InvoiceDto> getLastThreeApprovedInvoices(InvoiceStatus invoiceStatus);
}
