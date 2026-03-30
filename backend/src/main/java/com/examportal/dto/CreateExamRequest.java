package com.examportal.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class CreateExamRequest {
    @NotBlank private String title;
    private String description;
    @NotNull @Min(5)  private Integer durationMinutes;
    @NotNull @Min(1)  private Integer totalMarks;
    @NotNull @Min(1)  private Integer passMarks;
}
