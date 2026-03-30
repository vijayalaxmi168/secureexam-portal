package com.examportal.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class ViolationRequest {
    @NotNull  private Long   examId;
    @NotBlank private String violationType;
}
