package com.examportal.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class ViolationResponse {
    private int     totalViolations;
    private boolean terminated;
    private String  message;
}
