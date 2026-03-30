package com.examportal.controller;

import com.examportal.dto.*;
import com.examportal.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
@Tag(name = "Violations", description = "Anti-cheat violation recording")
@SecurityRequirement(name = "bearerAuth")
public class ViolationController {

    private final ExamService examService;

    /**
     * Called by the React frontend when:
     *  - Student switches tabs       → violationType = "TAB_SWITCH"
     *  - Student attempts copy/paste → violationType = "COPY_ATTEMPT"
     *
     * Response includes terminated=true when violation 3 is hit.
     * The frontend must then immediately force-submit with submitReason=FORCE_TERMINATED.
     */
    @PostMapping
    @Operation(summary = "Record an anti-cheat violation for the current student")
    public ResponseEntity<ApiResponse<ViolationResponse>> recordViolation(
            @Valid @RequestBody ViolationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ViolationResponse resp = examService.recordViolation(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(resp.getMessage(), resp));
    }
}
