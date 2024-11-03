package com.hnue.english.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUser {
    @NotBlank(message = "Không để trống tên")
    private String fullName;

    @NotBlank(message = "Không để trống email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Không để trống mật khẩu")
    private String password;
}
