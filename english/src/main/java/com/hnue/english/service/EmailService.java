package com.hnue.english.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public String sendOtpEmail(String to){
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        String formattedExpirationTime = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy").format(expirationTime);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Easy Vocab: LẤY LẠI MẬT KHẨU");
        message.setText("Mã OTP để bạn lấy lại mật khẩu là: " + otp + "\nMã sẽ hết hạn vào: " + formattedExpirationTime);
        mailSender.send(message);
        return otp;
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}
