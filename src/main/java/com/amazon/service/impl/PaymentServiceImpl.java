package com.amazon.service.impl;

import com.amazon.config.StripeConfig;
import com.amazon.dto.InvoiceDto;
import com.amazon.dto.PaymentDto;
import com.amazon.entity.Payment;
import com.amazon.enums.Months;
import com.amazon.exception.PaymentNotFoundException;
import com.amazon.repository.PaymentRepository;
import com.amazon.service.PaymentService;
import com.amazon.util.MapperUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MapperUtil mapperUtil;
    private final ModelMapper modelMapper;
    private StripeConfig stripeConfig;

    public PaymentServiceImpl(PaymentRepository paymentRepository, MapperUtil mapperUtil, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.mapperUtil = mapperUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public PaymentDto findById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapperUtil.convert(payment, new PaymentDto());
    }

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream()
                .map(payment -> mapperUtil.convert(payment, new PaymentDto()))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto save(PaymentDto paymentDto) {
        Payment payment = mapperUtil.convert(paymentDto, new Payment());
        Payment savedPayment = paymentRepository.save(payment);
        return mapperUtil.convert(savedPayment, new PaymentDto());
    }

    @Override
    public PaymentDto update(PaymentDto paymentDto) {
        Payment payment = mapperUtil.convert(paymentDto, new Payment());
        Payment updatedPayment = paymentRepository.save(payment);
        return mapperUtil.convert(updatedPayment, new PaymentDto());
    }

    @Override
    public void delete(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public List<PaymentDto> listPaymentsForYear(int year) {
        List<Payment> payments = paymentRepository.findByYear(year);


        if (payments.isEmpty()) {
            createPaymentsForYear(year);
            payments = paymentRepository.findByYear(year);
        }

        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .sorted(Comparator.comparing(payment -> payment.getMonth().ordinal()))
                .collect(Collectors.toList());
    }

    @Override
    public void payPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        paymentRepository.save(payment);
    }

    @Override
    public InvoiceDto getInvoiceForPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        return modelMapper.map(payment, InvoiceDto.class);
    }


    @Override
    public PaymentIntent payment(Long amount, Integer monthId) throws StripeException {

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(PaymentDto.Currency.USD.getValue())
                        .setDescription(LocalDate.now() + "  Cydeo Accounting Subscription Fee is Paid, Your Account Has Been Restored")
                        .build();

        Payment payment = paymentRepository.findByMonth(Months.values()[monthId - 1]);

        PaymentDto convertedPayment = mapperUtil.convert(payment, new PaymentDto());
        convertedPayment.setPaid(true);
        payment.setPaid(true);
        paymentRepository.save(payment);


        return PaymentIntent.create(params);
    }


    private void createPaymentsForYear(int year) {
        List<Payment> payments = new ArrayList<>();
        for (Months month : Months.values()) {
            Payment payment = new Payment();
            payment.setYear(year);
            payment.setMonth(month);
            payment.setAmount(BigDecimal.valueOf(250));
            payment.setPaid(false);
            payments.add(payment);
        }
        paymentRepository.saveAll(payments);
    }
}



