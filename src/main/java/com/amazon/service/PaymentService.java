package com.amazon.service;

import com.amazon.dto.InvoiceDto;
import com.amazon.dto.PaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import java.util.List;


public interface PaymentService {

    PaymentDto findById(Long id);

    List<PaymentDto> findAll();

    PaymentDto save(PaymentDto paymentDto);

    PaymentDto update(PaymentDto paymentDto);

    void delete(Long id);

    List<PaymentDto> listPaymentsForYear(int year);
    void payPayment(Long id);
    InvoiceDto getInvoiceForPayment(Long id);


     PaymentIntent payment(Long amount,Integer monthId) throws StripeException;
}