package com.hnue.english.service;

import com.hnue.english.config.VNPAYConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VNPAYService {
    private final VNPAYConfig vnpayConfig;

    public String createPaymentUrl(HttpServletRequest request, int paid){
        long amount = (long)Double.parseDouble(request.getParameter("amount"))*100;
        if (paid == 0){
            amount = amount * 80 / 100;
        }
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParams = vnpayConfig.getVNPayConfig();
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParams.put("vnp_BankCode", bankCode);
        }
        vnpParams.put("vnp_IpAddr", vnpayConfig.getIpAddress(request));
        String queryUrl = vnpayConfig.getPaymentURL(vnpParams, true);
        String hashData = vnpayConfig.getPaymentURL(vnpParams, false);
        String vnpSecureHash = vnpayConfig.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        return vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;
    }
}
