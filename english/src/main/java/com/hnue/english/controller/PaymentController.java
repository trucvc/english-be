package com.hnue.english.controller;

import com.hnue.english.response.ApiResponse;
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
    private final VNPAYService vnpayService;

    @GetMapping("/vn-pay")
    public ResponseEntity<ApiResponse<?>> pay(HttpServletRequest request){
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", vnpayService.createPaymentUrl(request)));
    }

    @GetMapping("/call-back")
    public ResponseEntity<ApiResponse<?>> callback(HttpServletRequest request){
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")){
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Thanh toán thành công",null));
        }else{
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Thanh toán thất bại", "Bad Request"));
        }
    }
}
