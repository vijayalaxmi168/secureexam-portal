import React, { useState, useEffect } from 'react';
import { getMyResults } from '../services/api';

export default function MyResultsPage({ user, onBack, onLogout }) {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    getMyResults()
      .then((res) => setResults(res.data.data || []))
      .catch(() => setError('Failed to load results.'))
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  const reasonBadge = (reason) => {
    const map = {
      MANUAL:           { label: 'Manual',      color: 'var(--accent)' },
      TIMER_EXPIRED:    { label: 'Time Up',     color: 'var(--warning)' },
      FORCE_TERMINATED: { label: 'Terminated',  color: 'var(--danger)' },
    };
    const r = map[reason] || { label: reason, color: 'var(--text-muted)' };
    return (
      <span style={{
        padding: '2px 10px', borderRadius: 12, fontSize: '0.75rem',
        fontWeight: 600, background: `${r.color}22`, color: r.color,
      }}>
        {r.label}
      </span>
    );
  };

  return (
    <div>
      {/* Navbar */}
      <nav className="navbar">
        <span className="navbar-brand">🛡️ SecureExam</span>
        <div className="navbar-right">
          <span className="text-muted" style={{ fontSize: '0.875rem' }}>👤 {user?.username}</span>
          <button className="btn btn-ghost" style={{ padding: '6px 14px', fontSize: '0.85rem' }} onClick={onBack}>
            ← Back
          </button>
          <button className="btn btn-ghost" style={{ padding: '6px 14px', fontSize: '0.85rem' }} onClick={onLogout}>
            Logout
          </button>
        </div>
      </nav>

      <div className="results-table-wrap">
        <h2 style={{ marginBottom: 6 }}>My Results</h2>
        <p className="text-muted" style={{ marginBottom: 24 }}>Your complete exam submission history.</p>

        {loading && <div className="spinner-wrap"><div className="spinner" /></div>}
        {error   && <div className="alert alert-error">{error}</div>}

        {!loading && !error && results.length === 0 && (
          <div style={{ textAlign: 'center', padding: '64px 0' }}>
            <p style={{ fontSize: '2rem', marginBottom: 12 }}>📋</p>
            <p className="text-muted">You haven't submitted any exams yet.</p>
          </div>
        )}

        {!loading && results.length > 0 && (
          <table className="results-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Exam</th>
                <th>Score</th>
                <th>Percentage</th>
                <th>Result</th>
                <th>Submitted</th>
                <th>Reason</th>
              </tr>
            </thead>
            <tbody>
              {results.map((r, i) => {
                const pct = Math.round((r.scoreObtained / r.totalMarks) * 100);
                return (
                  <tr key={r.id}>
                    <td className="text-muted">{i + 1}</td>
                    <td style={{ fontWeight: 500 }}>{r.examTitle}</td>
                    <td>{r.scoreObtained} / {r.totalMarks}</td>
                    <td>{pct}%</td>
                    <td>
                      <span className={`badge ${r.passed ? 'badge-pass' : 'badge-fail'}`}>
                        {r.passed ? 'PASS' : 'FAIL'}
                      </span>
                    </td>
                    <td className="text-muted" style={{ fontSize: '0.82rem' }}>
                      {formatDate(r.submittedAt)}
                    </td>
                    <td>{reasonBadge(r.submitReason)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
