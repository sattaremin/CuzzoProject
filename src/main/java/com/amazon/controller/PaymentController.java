package com.amazon.controller;


import com.amazon.dto.InvoiceDto;
import com.amazon.dto.PaymentDto;
import com.amazon.service.CompanyService;
import com.amazon.service.PaymentService;
import com.amazon.util.CompanyMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final String API_KEY = "pk_test_51PjD5wCQKQxTma0MK6PmIsDC57FwV9EClP6abhjUVuydQLAdmNELARaWImTJQQfWny5Jv82BwYIryUpAJiyVG3s700hXVEWaAd";

    private final PaymentService paymentService;
    private final CompanyMapper companyMapper;
    private final CompanyService  companyService;

    public PaymentController(PaymentService paymentService, CompanyMapper companyMapper, CompanyService companyService) {
        this.paymentService = paymentService;
        this.companyMapper = companyMapper;
        this.companyService = companyService;
    }


    @GetMapping("/list")
    public String listPayments(@RequestParam(name = "year", required = false) Integer year, Model model) {
        if (year == null) {
            year = Year.now().getValue();
        }
        List<PaymentDto> payments = paymentService.listPaymentsForYear(year);
        model.addAttribute("year", year);
        model.addAttribute("payments", payments);
        return "payment/payment-list";
    }

    @GetMapping("/newpayment/{id}")
    public String payPayment(@PathVariable Long id, Model model) throws StripeException {



        PaymentDto payment = paymentService.findById(id);
        payment.setAmount(new BigDecimal("250.00"));
        model.addAttribute("payment", payment);
        model.addAttribute("stripePublicKey", API_KEY);
        model.addAttribute("monthId", payment.getMonth().getId());
        model.addAttribute("currency", PaymentDto.Currency.USD.getValue());
        paymentService.payPayment(id);
        return "payment/payment-method";
    }

    @GetMapping("/toInvoice/{id}")
    public String getInvoice(@PathVariable Long id, Model model) {
        InvoiceDto invoice = paymentService.getInvoiceForPayment(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("company",companyMapper.convertToEntity(companyService.getCompanyDtoByLoggedInUser()));
        model.addAttribute("payment",paymentService.findById(id));
        return "payment/payment-invoice-print";
    }

    @PostMapping("/charge/{id}")
    public String chargeResult(@PathVariable("id") Integer id, Model model) throws StripeException {
            PaymentIntent paymentIntent = paymentService.payment(25000L, id); //
            model.addAttribute("description", paymentIntent.getDescription());
            model.addAttribute("chargeId", paymentIntent.getId());
        return "/payment/payment-result";
    }



}
