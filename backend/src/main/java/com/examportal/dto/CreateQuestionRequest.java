package com.examportal.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class CreateQuestionRequest {
    @NotNull  private Long    examId;
    @NotBlank private String  questionText;
    @NotBlank private String  optionA;
    @NotBlank private String  optionB;
    @NotBlank private String  optionC;
    @NotBlank private String  optionD;
    @NotBlank @Pattern(regexp = "[ABCD]", message = "Must be A, B, C or D")
    private String correctOption;
    @NotNull @Min(1) private Integer marks;
}
