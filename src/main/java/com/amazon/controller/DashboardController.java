package com.amazon.controller;

import com.amazon.client.CurrencyClient;
import com.amazon.enums.InvoiceStatus;
import com.amazon.service.DashBoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {


    private final DashBoardService dashBoardService;
    private final CurrencyClient currencyClient;

    public DashboardController(DashBoardService dashBoardService, CurrencyClient currencyClient) {

        this.dashBoardService = dashBoardService;
        this.currencyClient = currencyClient;
    }

    @GetMapping
    public String showDashboard(Model model) {
        model.addAttribute("invoices", dashBoardService.getLastThreeApprovedInvoices(InvoiceStatus.APPROVED));
        Map<String, BigDecimal> summaryNumbers = dashBoardService.getSummaryNumbers();
        model.addAttribute("summaryNumbers", summaryNumbers);
        model.addAttribute("exchangeRates", currencyClient.getCurrency().getExchangeRate());

        return "dashboard";
    }

   
}




