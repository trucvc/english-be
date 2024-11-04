package com.hnue.english.dto;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String email;
    private String password;
    private String fullName;
    private String subscriptionPlan;
    private String role;
    private int paid;
}
