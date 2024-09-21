package com.amazon.repository;

import com.amazon.entity.Payment;
import com.amazon.enums.Months;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByYear(int year);
    Payment findByMonth(Months month);
}