package com.examportal.repository;

import com.examportal.entity.ViolationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViolationAuditRepository extends JpaRepository<ViolationAudit, Long> {
    long countByUserIdAndExamId(Long userId, Long examId);
    List<ViolationAudit> findByUserIdAndExamId(Long userId, Long examId);
}
