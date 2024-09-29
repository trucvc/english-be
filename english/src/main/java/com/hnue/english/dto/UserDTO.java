package com.hnue.english.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty("email")
    @NotBlank(message = "Không để trống email")
    @Email(message = "Phải có định dạng là email")
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "Không để trống mật khẩu")
    private String password;

}
