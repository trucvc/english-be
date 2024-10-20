package com.hnue.english.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T>{
    private int statusCode;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public static <T> ApiResponse<T> success(int statusCode ,String message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> error(int statusCode, String message, String error) {
        return ApiResponse.<Void>builder()
                .statusCode(statusCode)
                .message(message)
                .data(null)
                .error(error)
                .build();
    }
}
