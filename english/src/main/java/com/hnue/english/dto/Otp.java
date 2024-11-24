package com.hnue.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    private String email;
    private String otp;
    private LocalDateTime expiryTime;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
