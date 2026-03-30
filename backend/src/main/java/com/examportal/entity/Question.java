package com.examportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false, length = 300)
    private String optionA;

    @Column(nullable = false, length = 300)
    private String optionB;

    @Column(nullable = false, length = 300)
    private String optionC;

    @Column(nullable = false, length = 300)
    private String optionD;

    /**
     * NEVER exposed in QuestionResponse DTO — only read server-side for scoring.
     * Values: "A", "B", "C", or "D"
     */
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(nullable = false)
    private Integer marks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
}
