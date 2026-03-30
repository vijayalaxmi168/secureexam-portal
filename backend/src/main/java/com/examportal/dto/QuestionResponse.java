package com.examportal.dto;
import lombok.*;
/**
 * SECURITY: correctOption is deliberately absent.
 * Even if the HTTP response is intercepted, answers are never exposed.
 */
@Getter @AllArgsConstructor
public class QuestionResponse {
    private Long    id;
    private String  questionText;
    private String  optionA;
    private String  optionB;
    private String  optionC;
    private String  optionD;
    private Integer marks;
}
