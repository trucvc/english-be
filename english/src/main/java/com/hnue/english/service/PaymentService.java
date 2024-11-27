package com.hnue.english.service;

import com.hnue.english.model.Payment;
import com.hnue.english.model.User;
import com.hnue.english.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public void createPayment(User user, double price){
        Payment payment = new Payment();
        payment.setPrice(price);
        payment.setUser(user);
        payment.setCreateAt(new Date());
        paymentRepository.save(payment);
    }

    public BigDecimal revenue(){
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment -> new BigDecimal(payment.getPrice()).setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
