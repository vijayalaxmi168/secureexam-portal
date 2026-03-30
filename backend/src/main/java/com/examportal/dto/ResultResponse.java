package com.examportal.dto;
import lombok.*;
import java.time.LocalDateTime;
@Getter @AllArgsConstructor
public class ResultResponse {
    private Long          id;
    private String        examTitle;
    private Integer       scoreObtained;
    private Integer       totalMarks;
    private Boolean       passed;
    private String        submitReason;
    private LocalDateTime submittedAt;
}
