package com.examportal.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class ExamSummaryResponse {
    private Long    id;
    private String  title;
    private String  description;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passMarks;
}
