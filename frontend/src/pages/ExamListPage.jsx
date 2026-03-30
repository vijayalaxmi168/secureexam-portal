import React, { useState, useEffect } from 'react';
import { getExams } from '../services/api';

export default function ExamListPage({ user, onStartExam, onViewResults, onLogout }) {
  const [exams, setExams]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    getExams()
      .then((res) => setExams(res.data.data || []))
      .catch(() => setError('Failed to load exams. Please refresh.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      {/* Navbar */}
      <nav className="navbar">
        <span className="navbar-brand">🛡️ SecureExam</span>
        <div className="navbar-right">
          <span className="text-muted" style={{ fontSize: '0.875rem' }}>
            👤 {user?.username}
            <span style={{ marginLeft: 8, padding: '2px 8px', background: 'var(--bg-input)', borderRadius: 4, fontSize: '0.75rem' }}>
              {user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Student'}
            </span>
          </span>
          <button className="btn btn-ghost" style={{ padding: '6px 14px', fontSize: '0.85rem' }} onClick={onViewResults}>
            My Results
          </button>
          <button className="btn btn-ghost" style={{ padding: '6px 14px', fontSize: '0.85rem' }} onClick={onLogout}>
            Logout
          </button>
        </div>
      </nav>

      {/* Page header */}
      <div style={{ padding: '32px 32px 0' }}>
        <h2>Available Exams</h2>
        <p className="text-muted">Select an exam to begin. Each exam is individually proctored.</p>
      </div>

      {/* States */}
      {loading && (
        <div className="spinner-wrap">
          <div className="spinner" />
        </div>
      )}
      {error && (
        <div style={{ padding: '32px' }}>
          <div className="alert alert-error">{error}</div>
        </div>
      )}

      {/* Exam grid */}
      {!loading && !error && (
        <>
          {exams.length === 0 ? (
            <div style={{ padding: '64px', textAlign: 'center' }}>
              <p className="text-muted">No active exams available right now.</p>
            </div>
          ) : (
            <div className="exam-grid">
              {exams.map((exam) => (
                <div className="exam-card" key={exam.id}>
                  <h3>{exam.title}</h3>
                  <p className="text-muted" style={{ fontSize: '0.875rem' }}>
                    {exam.description}
                  </p>
                  <div className="exam-meta">
                    <span>⏱ {exam.durationMinutes} min</span>
                    <span>📝 {exam.totalMarks} marks</span>
                    <span>🎯 Pass: {exam.passMarks}</span>
                  </div>
                  <div style={{ marginTop: 8 }}>
                    <button
                      className="btn btn-primary"
                      style={{ marginTop: 0 }}
                      onClick={() => onStartExam(exam.id)}
                    >
                      Start Exam →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* Anti-cheat notice */}
      <div style={{ padding: '0 32px 32px', marginTop: 16 }}>
        <div className="alert alert-warning" style={{ maxWidth: 600 }}>
          ⚠️ <strong>Proctoring is active.</strong> Tab switching, copy, and paste are monitored.
          3 violations will automatically terminate your exam session.
        </div>
      </div>
    </div>
  );
}
