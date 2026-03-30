import React, { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import ExamListPage from './pages/ExamListPage';
import ExamRoomPage from './pages/ExamRoomPage';
import ResultPage from './pages/ResultPage';
import MyResultsPage from './pages/MyResultsPage';

// Simple client-side router using a state machine — no react-router needed
// Pages: 'auth' | 'examList' | 'examRoom' | 'result' | 'myResults'

export default function App() {
  const [page, setPage]           = useState('auth');
  const [user, setUser]           = useState(null);
  const [activeExamId, setActiveExamId] = useState(null);
  const [lastResult, setLastResult]     = useState(null);

  // Rehydrate session on page refresh
  useEffect(() => {
    const token    = localStorage.getItem('accessToken');
    const username = localStorage.getItem('username');
    const role     = localStorage.getItem('role');
    if (token && username) {
      setUser({ username, role });
      setPage('examList');
    }
  }, []);

  // Disable right-click and copy/paste globally
  useEffect(() => {
    const block = (e) => e.preventDefault();
    document.addEventListener('contextmenu', block);
    document.addEventListener('copy',  block);
    document.addEventListener('paste', block);
    document.addEventListener('cut',   block);
    return () => {
      document.removeEventListener('contextmenu', block);
      document.removeEventListener('copy',  block);
      document.removeEventListener('paste', block);
      document.removeEventListener('cut',   block);
    };
  }, []);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    setPage('examList');
  };

  const handleStartExam = (examId) => {
    setActiveExamId(examId);
    setPage('examRoom');
  };

  const handleExamFinished = (result) => {
    setLastResult(result);
    setActiveExamId(null);
    setPage('result');
  };

  const handleLogout = () => {
    localStorage.clear();
    setUser(null);
    setPage('auth');
  };

  if (page === 'auth')       return <LoginPage onLogin={handleLoginSuccess} />;
  if (page === 'examRoom')   return <ExamRoomPage examId={activeExamId} onFinish={handleExamFinished} />;
  if (page === 'result')     return <ResultPage result={lastResult} onBack={() => setPage('examList')} />;
  if (page === 'myResults')  return <MyResultsPage user={user} onBack={() => setPage('examList')} onLogout={handleLogout} />;

  return (
    <ExamListPage
      user={user}
      onStartExam={handleStartExam}
      onViewResults={() => setPage('myResults')}
      onLogout={handleLogout}
    />
  );
}
