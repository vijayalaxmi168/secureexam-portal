package com.examportal.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
