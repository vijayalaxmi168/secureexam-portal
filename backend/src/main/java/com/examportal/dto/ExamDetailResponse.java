package com.examportal.dto;
import lombok.*;
import java.util.List;
@Getter @AllArgsConstructor
public class ExamDetailResponse {
    private Long                  id;
    private String                title;
    private Integer               durationMinutes;
    private Integer               totalMarks;
    private List<QuestionResponse> questions;
}
