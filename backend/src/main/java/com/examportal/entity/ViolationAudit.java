package com.examportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "violation_audits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ViolationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /**
     * Type of violation: TAB_SWITCH, COPY_ATTEMPT, PASTE_ATTEMPT, etc.
     */
    @Column(name = "violation_type", nullable = false, length = 50)
    private String violationType;

    @Column(name = "occurred_at", updatable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onRecord() {
        this.occurredAt = LocalDateTime.now();
    }
}
