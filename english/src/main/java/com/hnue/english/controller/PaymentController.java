package com.hnue.english.controller;

import com.hnue.english.model.User;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.PaymentService;
import com.hnue.english.service.UserService;
import com.hnue.english.service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final UserService userService;
    private final VNPAYService vnpayService;
    private final PaymentService paymentService;

    @GetMapping("/pay")
    public ResponseEntity<ApiResponse<?>> pay(HttpServletRequest request){
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", vnpayService.createPaymentUrl(request)));
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<?>> callback(HttpServletRequest request){
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")){
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            double amount = Double.parseDouble(request.getParameter("vnp_Amount"))/100;
            if (amount == 299000 || amount == 499000){
                user.setSubscriptionPlan("6_months");
            } else if (amount == 699000 || amount == 899000) {
                user.setSubscriptionPlan("1_year");
            }else {
                user.setSubscriptionPlan("3_years");
            }
            User u = userService.payment(user);
            paymentService.createPayment(user, amount);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Thanh toán thành công",u));
        }else{
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Thanh toán thất bại", "Bad Request"));
        }
    }
}
