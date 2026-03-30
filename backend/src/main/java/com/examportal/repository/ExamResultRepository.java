package com.examportal.repository;

import com.examportal.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByUserId(Long userId);
    Optional<ExamResult> findByUserIdAndExamId(Long userId, Long examId);
    boolean existsByUserIdAndExamId(Long userId, Long examId);
}
