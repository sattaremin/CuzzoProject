package com.amazon.controller;


import com.amazon.service.ReportingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportingController {

    private final ReportingService reportingService;


    public ReportingController( ReportingService reportingService) {

        this.reportingService = reportingService;
    }

    @GetMapping("/profitLossData")
    public String getProfitLossReport(Model model) {
        Map<String, BigDecimal> monthlyProfitLossDataMap = reportingService.getMonthlyProfitLoss();
        model.addAttribute("monthlyProfitLossDataMap", monthlyProfitLossDataMap);
        return "report/profit-loss-report";
    }


    @GetMapping("/stockData")
    public String stockReport(Model model) {

        model.addAttribute("invoiceProducts", reportingService.getStockDetails());
        return "/report/stock-report";
    }
}
