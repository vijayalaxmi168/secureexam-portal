package com.examportal.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String  message;
    private T       data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
