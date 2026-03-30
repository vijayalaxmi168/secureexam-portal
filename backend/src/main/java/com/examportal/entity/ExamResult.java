package com.examportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Named ExamResult (not Result) to avoid collision with java.lang or Spring types.
 */
@Entity
@Table(name = "exam_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "score_obtained", nullable = false)
    private Integer scoreObtained;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "submit_reason", nullable = false)
    private SubmitReason submitReason;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onSubmit() {
        this.submittedAt = LocalDateTime.now();
    }

    public enum SubmitReason {
        MANUAL,           // Student clicked Submit
        TIMER_EXPIRED,    // 30-min timer hit zero
        FORCE_TERMINATED  // 3 violations → auto-terminated
    }
}
