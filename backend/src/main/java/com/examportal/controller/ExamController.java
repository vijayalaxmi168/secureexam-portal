package com.examportal.controller;

import com.examportal.dto.*;
import com.examportal.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Exam lifecycle for admins and students")
@SecurityRequirement(name = "bearerAuth")
public class ExamController {

    private final ExamService examService;

    /* ── Admin ──────────────────────────────────────────────────────────── */

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "[ADMIN] Create a new exam")
    public ResponseEntity<ApiResponse<Object>> createExam(
            @Valid @RequestBody CreateExamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Exam created", examService.createExam(request)));
    }

    @PostMapping("/questions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "[ADMIN] Add a question to an exam")
    public ResponseEntity<ApiResponse<Object>> addQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Question added", examService.addQuestion(request)));
    }

    /* ── Student ────────────────────────────────────────────────────────── */

    @GetMapping
    @Operation(summary = "List all active exams (no questions)")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>> getAllExams() {
        return ResponseEntity.ok(ApiResponse.ok(examService.getAllActiveExams()));
    }

    @GetMapping("/{examId}/start")
    @Operation(summary = "Start exam — returns questions WITHOUT correct answers")
    public ResponseEntity<ApiResponse<ExamDetailResponse>> startExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok(examService.getExamForStudent(examId, userDetails.getUsername())));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit answers — scores and saves result")
    public ResponseEntity<ApiResponse<ResultResponse>> submitExam(
            @Valid @RequestBody SubmitExamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("Submitted", examService.submitExam(request, userDetails.getUsername())));
    }

    @GetMapping("/my-results")
    @Operation(summary = "Get all results for the logged-in student")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> myResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok(examService.getMyResults(userDetails.getUsername())));
    }
}
