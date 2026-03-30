package com.examportal.service;

import com.examportal.dto.*;
import com.examportal.entity.*;
import com.examportal.exception.*;
import com.examportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository          examRepository;
    private final QuestionRepository      questionRepository;
    private final ExamResultRepository    examResultRepository;
    private final ViolationAuditRepository violationRepository;
    private final UserRepository          userRepository;

    @Value("${app.exam.max-violations}")
    private int maxViolations;

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Transactional
    public Exam createExam(CreateExamRequest req) {
        Exam exam = Exam.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .durationMinutes(req.getDurationMinutes())
                .totalMarks(req.getTotalMarks())
                .passMarks(req.getPassMarks())
                .isActive(true)
                .build();
        return examRepository.save(exam);
    }

    @Transactional
    public Question addQuestion(CreateQuestionRequest req) {
        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + req.getExamId()));

        Question q = Question.builder()
                .exam(exam)
                .questionText(req.getQuestionText())
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionC(req.getOptionC())
                .optionD(req.getOptionD())
                .correctOption(req.getCorrectOption())
                .marks(req.getMarks())
                .build();
        return questionRepository.save(q);
    }

    // ── Student: browse ───────────────────────────────────────────────────────

    public List<ExamSummaryResponse> getAllActiveExams() {
        return examRepository.findByIsActiveTrue().stream()
                .map(e -> new ExamSummaryResponse(
                        e.getId(), e.getTitle(), e.getDescription(),
                        e.getDurationMinutes(), e.getTotalMarks(), e.getPassMarks()))
                .collect(Collectors.toList());
    }

    /**
     * Loads questions for a student — correctOption is NEVER included in QuestionResponse.
     * Two guards run before questions are served:
     *  1. Already submitted → reject
     *  2. Violation limit hit → reject
     */
    public ExamDetailResponse getExamForStudent(Long examId, String username) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (examResultRepository.existsByUserIdAndExamId(user.getId(), examId))
            throw new ExamTerminatedException("You have already submitted this exam.");

        enforceViolationLimit(user.getId(), examId);

        List<QuestionResponse> questions = questionRepository.findByExamId(examId).stream()
                .map(q -> new QuestionResponse(
                        q.getId(), q.getQuestionText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                        q.getMarks()))    // correctOption intentionally excluded
                .collect(Collectors.toList());

        return new ExamDetailResponse(exam.getId(), exam.getTitle(),
                exam.getDurationMinutes(), exam.getTotalMarks(), questions);
    }

    // ── Student: submit ───────────────────────────────────────────────────────

    /**
     * Submission flow:
     *  1. Reject if already submitted (idempotency guard)
     *  2. Reject if violation limit exceeded
     *  3. Pull correctOption from DB (never from client!)
     *  4. Score = sum of marks for correctly answered questions
     *  5. Persist ExamResult
     */
    @Transactional
    public ResultResponse submitExam(SubmitExamRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + req.getExamId()));

        if (examResultRepository.existsByUserIdAndExamId(user.getId(), exam.getId()))
            throw new ExamTerminatedException("Exam already submitted.");

        enforceViolationLimit(user.getId(), exam.getId());

        // Score by fetching correct answers from DB — client has no say in this
        List<Question>   questions      = questionRepository.findByExamId(exam.getId());
        Map<Long, String> clientAnswers = req.getAnswers();

        int score = questions.stream()
                .filter(q -> {
                    String submitted = clientAnswers.get(q.getId());
                    return submitted != null
                            && submitted.equalsIgnoreCase(q.getCorrectOption());
                })
                .mapToInt(Question::getMarks)
                .sum();

        boolean passed = score >= exam.getPassMarks();

        ExamResult result = ExamResult.builder()
                .user(user)
                .exam(exam)
                .scoreObtained(score)
                .totalMarks(exam.getTotalMarks())
                .passed(passed)
                .submitReason(ExamResult.SubmitReason.valueOf(req.getSubmitReason()))
                .build();

        ExamResult saved = examResultRepository.save(result);

        return new ResultResponse(
                saved.getId(), exam.getTitle(),
                saved.getScoreObtained(), saved.getTotalMarks(),
                saved.getPassed(), saved.getSubmitReason().name(),
                saved.getSubmittedAt());
    }

    public List<ResultResponse> getMyResults(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return examResultRepository.findByUserId(user.getId()).stream()
                .map(r -> new ResultResponse(
                        r.getId(), r.getExam().getTitle(),
                        r.getScoreObtained(), r.getTotalMarks(),
                        r.getPassed(), r.getSubmitReason().name(),
                        r.getSubmittedAt()))
                .collect(Collectors.toList());
    }

    // ── Anti-Cheat ────────────────────────────────────────────────────────────

    /**
     * recordViolation:
     *  - Persists the violation event with type + timestamp
     *  - Counts total violations for this user+exam
     *  - If count >= maxViolations (3), sets terminated=true
     *  - Frontend MUST force-submit when terminated=true
     */
    @Transactional
    public ViolationResponse recordViolation(ViolationRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + req.getExamId()));

        ViolationAudit audit = ViolationAudit.builder()
                .user(user)
                .exam(exam)
                .violationType(req.getViolationType())
                .build();
        violationRepository.save(audit);

        long total      = violationRepository.countByUserIdAndExamId(user.getId(), exam.getId());
        boolean terminated = total >= maxViolations;

        String message = terminated
                ? "EXAM TERMINATED: You have exceeded the maximum allowed violations."
                : String.format("Warning %d of %d: suspicious activity recorded.", total, maxViolations);

        return new ViolationResponse((int) total, terminated, message);
    }

    /**
     * Shared guard used by both getExamForStudent and submitExam.
     * Ensures a terminated student cannot load questions OR submit answers.
     */
    private void enforceViolationLimit(Long userId, Long examId) {
        long violations = violationRepository.countByUserIdAndExamId(userId, examId);
        if (violations >= maxViolations) {
            throw new ExamTerminatedException(
                    "Exam access denied: violation limit (" + maxViolations + ") exceeded.");
        }
    }
}
