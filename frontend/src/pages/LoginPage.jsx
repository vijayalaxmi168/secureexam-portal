import React, { useState } from 'react';
import { login, register } from '../services/api';

export default function LoginPage({ onLogin }) {
  const [mode, setMode]       = useState('login'); // 'login' | 'register'
  const [form, setForm]       = useState({ username: '', email: '', password: '' });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let res;
      if (mode === 'login') {
        res = await login({ username: form.username, password: form.password });
      } else {
        res = await register({ username: form.username, email: form.email, password: form.password });
      }

      const { accessToken, refreshToken, username, role } = res.data.data;
      localStorage.setItem('accessToken',  accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('username',     username);
      localStorage.setItem('role',         role);

      onLogin({ username, role });
    } catch (err) {
      const msg = err.response?.data?.message || 'Something went wrong. Try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setMode(mode === 'login' ? 'register' : 'login');
    setError('');
    setForm({ username: '', email: '', password: '' });
  };

  return (
    <div className="page-center">
      <div className="card">
        <div className="auth-logo">
          <span className="logo-icon">🛡️</span>
          <h1>SecureExam</h1>
          <p className="text-muted">Proctored Online Examination Portal</p>
        </div>

        <h2>{mode === 'login' ? 'Sign In' : 'Create Account'}</h2>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input
              name="username"
              type="text"
              placeholder="Enter username"
              value={form.username}
              onChange={handleChange}
              required
              autoComplete="off"
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label>Email</label>
              <input
                name="email"
                type="email"
                placeholder="Enter email"
                value={form.email}
                onChange={handleChange}
                required
              />
            </div>
          )}

          <div className="form-group">
            <label>Password</label>
            <input
              name="password"
              type="password"
              placeholder="Enter password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Please wait…' : mode === 'login' ? 'Sign In' : 'Register'}
          </button>
        </form>

        <div className="auth-switch">
          {mode === 'login' ? (
            <span>Don't have an account? <a onClick={switchMode}>Register</a></span>
          ) : (
            <span>Already have an account? <a onClick={switchMode}>Sign In</a></span>
          )}
        </div>

        {mode === 'login' && (
          <>
            <hr className="divider" />
            <p className="text-muted" style={{ fontSize: '0.8rem', textAlign: 'center' }}>
              Demo → <strong style={{ color: 'var(--text)' }}>student1 / student123</strong>
            </p>
          </>
        )}
      </div>
    </div>
  );
}
