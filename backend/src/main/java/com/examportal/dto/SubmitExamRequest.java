package com.examportal.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Map;
@Getter @Setter
public class SubmitExamRequest {
    @NotNull  private Long             examId;
    @NotNull  private Map<Long,String> answers;
    @NotBlank private String           submitReason;
}
