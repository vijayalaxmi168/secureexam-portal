package com.examportal.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
}
