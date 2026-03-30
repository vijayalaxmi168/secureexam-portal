package com.examportal.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter
public class RefreshRequest {
    @NotBlank private String refreshToken;
}
