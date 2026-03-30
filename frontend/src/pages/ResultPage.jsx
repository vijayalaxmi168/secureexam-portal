import React from 'react';

export default function ResultPage({ result, onBack }) {
  if (!result) {
    return (
      <div className="page-center">
        <div className="card" style={{ textAlign: 'center' }}>
          <p className="text-muted">No result data found.</p>
          <button className="btn btn-primary" onClick={onBack}>Back to Exams</button>
        </div>
      </div>
    );
  }

  const percentage = Math.round((result.scoreObtained / result.totalMarks) * 100);
  const passed     = result.passed;

  const reasonLabel = {
    MANUAL:           'Submitted manually',
    TIMER_EXPIRED:    '⏱ Time expired — auto submitted',
    FORCE_TERMINATED: '🚫 Terminated due to violations',
  };

  return (
    <div className="page-center">
      <div className="card card-wide result-card">

        {/* Score circle */}
        <div className={`score-circle ${passed ? 'passed' : 'failed'}`}>
          <span className="score-big">{percentage}%</span>
          <span className="score-label">{result.scoreObtained}/{result.totalMarks}</span>
        </div>

        {/* Pass/Fail badge */}
        <h1 className={passed ? 'text-success' : 'text-danger'}>
          {passed ? '🎉 Passed!' : '❌ Failed'}
        </h1>

        <p className="text-muted" style={{ marginTop: 8 }}>
          {result.examTitle}
        </p>

        <hr className="divider" />

        {/* Details */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, textAlign: 'left', marginBottom: 28 }}>
          <div>
            <p className="text-muted" style={{ fontSize: '0.8rem', marginBottom: 4 }}>Score</p>
            <p style={{ fontWeight: 700, fontSize: '1.1rem' }}>
              {result.scoreObtained} / {result.totalMarks}
            </p>
          </div>
          <div>
            <p className="text-muted" style={{ fontSize: '0.8rem', marginBottom: 4 }}>Percentage</p>
            <p style={{ fontWeight: 700, fontSize: '1.1rem' }}>{percentage}%</p>
          </div>
          <div>
            <p className="text-muted" style={{ fontSize: '0.8rem', marginBottom: 4 }}>Status</p>
            <span className={`badge ${passed ? 'badge-pass' : 'badge-fail'}`}>
              {passed ? 'PASSED' : 'FAILED'}
            </span>
          </div>
          <div>
            <p className="text-muted" style={{ fontSize: '0.8rem', marginBottom: 4 }}>Submission</p>
            <p style={{ fontSize: '0.875rem' }}>
              {reasonLabel[result.submitReason] || result.submitReason}
            </p>
          </div>
        </div>

        <button className="btn btn-primary" style={{ maxWidth: 280, margin: '0 auto' }} onClick={onBack}>
          ← Back to Exam List
        </button>
      </div>
    </div>
  );
}
