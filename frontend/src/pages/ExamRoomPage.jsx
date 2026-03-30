import React, { useState, useEffect, useRef, useCallback } from 'react';
import { startExam, submitExam, reportViolation } from '../services/api';

const TOTAL_SECONDS = 30 * 60; // 30 minutes

export default function ExamRoomPage({ examId, onFinish }) {
  const [examData, setExamData]       = useState(null);
  const [answers, setAnswers]         = useState({});     // { questionId: 'A'|'B'|'C'|'D' }
  const [timeLeft, setTimeLeft]       = useState(TOTAL_SECONDS);
  const [loading, setLoading]         = useState(true);
  const [submitting, setSubmitting]   = useState(false);
  const [error, setError]             = useState('');
  const [violations, setViolations]   = useState(0);
  const [violationMsg, setViolationMsg] = useState('');
  const [terminated, setTerminated]   = useState(false);

  // Refs to avoid stale closures in event listeners
  const examIdRef    = useRef(examId);
  const answersRef   = useRef(answers);
  const terminatedRef = useRef(false);
  const submitCalledRef = useRef(false);

  useEffect(() => { answersRef.current = answers; }, [answers]);

  // ── Load exam ──────────────────────────────────────────────────────────────
  useEffect(() => {
    startExam(examId)
      .then((res) => {
        setExamData(res.data.data);
        setTimeLeft(res.data.data.durationMinutes * 60);
      })
      .catch((err) => {
        const msg = err.response?.data?.message || 'Failed to load exam.';
        setError(msg);
      })
      .finally(() => setLoading(false));
  }, [examId]);

  // ── Submission logic (memoised so timer can call it) ──────────────────────
  const handleSubmit = useCallback(async (reason = 'MANUAL') => {
    if (submitCalledRef.current) return;
    submitCalledRef.current = true;
    setSubmitting(true);

    try {
      const payload = {
        examId,
        answers: answersRef.current,
        submitReason: reason,
      };
      const res = await submitExam(payload);
      onFinish(res.data.data);
    } catch (err) {
      const msg = err.response?.data?.message || 'Submission failed. Please try again.';
      setError(msg);
      submitCalledRef.current = false; // allow retry
    } finally {
      setSubmitting(false);
    }
  }, [examId, onFinish]);

  // ── Countdown timer ────────────────────────────────────────────────────────
  useEffect(() => {
    if (!examData) return;

    const interval = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          if (!terminatedRef.current) {
            handleSubmit('TIMER_EXPIRED');
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [examData, handleSubmit]);

  // ── Anti-cheat: tab visibility ─────────────────────────────────────────────
  useEffect(() => {
    const handleVisibilityChange = async () => {
      if (document.hidden && !terminatedRef.current) {
        try {
          const res = await reportViolation(examIdRef.current, 'TAB_SWITCH');
          const data = res.data.data;
          setViolations(data.totalViolations);
          setViolationMsg(data.message);

          // Show warning banner for 3 seconds
          setTimeout(() => setViolationMsg(''), 3000);

          if (data.terminated) {
            terminatedRef.current = true;
            setTerminated(true);
            // Small delay so the student sees the message, then force-submit
            setTimeout(() => handleSubmit('FORCE_TERMINATED'), 2500);
          }
        } catch {
          // Silent — network failure shouldn't break the exam
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [handleSubmit]);

  // ── Format timer display ───────────────────────────────────────────────────
  const formatTime = (secs) => {
    const m = Math.floor(secs / 60).toString().padStart(2, '0');
    const s = (secs % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  };

  const timerClass = () => {
    if (timeLeft <= 60)  return 'timer danger';
    if (timeLeft <= 300) return 'timer warning';
    return 'timer';
  };

  const answeredCount = Object.keys(answers).length;
  const totalQuestions = examData?.questions?.length || 0;

  // ── Render states ──────────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="page-center">
        <div>
          <div className="spinner-wrap"><div className="spinner" /></div>
          <p className="text-muted" style={{ textAlign: 'center', marginTop: 16 }}>
            Loading exam…
          </p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-center">
        <div className="card">
          <div className="alert alert-error">{error}</div>
          <button className="btn btn-ghost" onClick={() => window.location.reload()}>
            Go Back
          </button>
        </div>
      </div>
    );
  }

  if (terminated) {
    return (
      <div className="page-center">
        <div className="card" style={{ textAlign: 'center' }}>
          <p style={{ fontSize: '3rem', marginBottom: 16 }}>🚫</p>
          <h2 className="text-danger">Exam Terminated</h2>
          <p className="text-muted" style={{ marginTop: 8 }}>
            You exceeded the maximum allowed violations. Your answers are being submitted.
          </p>
          <div className="spinner-wrap" style={{ minHeight: 80 }}>
            <div className="spinner" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>

      {/* Violation banner */}
      {violationMsg && (
        <div className="violation-banner">
          ⚠️ {violationMsg}
        </div>
      )}

      {/* Sticky header */}
      <div className="exam-header">
        <div>
          <div className="exam-title">{examData?.title}</div>
          <div className="text-muted" style={{ fontSize: '0.8rem', marginTop: 2 }}>
            Violations: {violations} / 3 &nbsp;|&nbsp; Answered: {answeredCount}/{totalQuestions}
          </div>
        </div>
        <div className={timerClass()}>{formatTime(timeLeft)}</div>
      </div>

      {/* Questions */}
      <div className="exam-body" style={{ flex: 1 }}>
        {examData?.questions?.map((q, index) => (
          <div className="question-card" key={q.id}>
            <div className="question-number">Question {index + 1} of {totalQuestions} · {q.marks} mark{q.marks > 1 ? 's' : ''}</div>
            <div className="question-text">{q.questionText}</div>

            <div className="options-grid">
              {['A', 'B', 'C', 'D'].map((key) => {
                const optionText = q[`option${key}`];
                const isSelected = answers[q.id] === key;
                return (
                  <label
                    key={key}
                    className={`option-label ${isSelected ? 'selected' : ''}`}
                    onClick={() => setAnswers({ ...answers, [q.id]: key })}
                  >
                    <input
                      type="radio"
                      name={`q-${q.id}`}
                      value={key}
                      checked={isSelected}
                      onChange={() => setAnswers({ ...answers, [q.id]: key })}
                    />
                    <span className="option-key">{key}</span>
                    <span>{optionText}</span>
                  </label>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Sticky submit bar */}
      <div className="submit-bar">
        <span className="answered-count">
          {answeredCount} of {totalQuestions} answered
          {answeredCount < totalQuestions && (
            <span className="text-warning"> — {totalQuestions - answeredCount} unanswered</span>
          )}
        </span>
        <button
          className="btn btn-success"
          onClick={() => {
            const unanswered = totalQuestions - answeredCount;
            const confirmMsg = unanswered > 0
              ? `You have ${unanswered} unanswered question(s). Submit anyway?`
              : 'Submit your exam now?';
            if (window.confirm(confirmMsg)) handleSubmit('MANUAL');
          }}
          disabled={submitting}
        >
          {submitting ? 'Submitting…' : '✔ Submit Exam'}
        </button>
      </div>
    </div>
  );
}
