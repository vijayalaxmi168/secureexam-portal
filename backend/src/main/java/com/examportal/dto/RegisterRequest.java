package com.examportal.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class RegisterRequest {
    @NotBlank(message = "Username is required") @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Email(message = "Invalid email")
    private String email;
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
