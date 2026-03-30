import axios from 'axios';

const BASE_URL = 'http://localhost:9090';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// ── Request interceptor: attach JWT to every request ─────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor: auto-refresh on 401 ────────────────────────────────
let isRefreshing = false;
let failedQueue  = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else       prom.resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue this request until the refresh completes
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        logout();
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken });
        const newAccessToken = data.data.accessToken;
        localStorage.setItem('accessToken',  newAccessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);

        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
        processQueue(null, newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        logout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

function logout() {
  localStorage.clear();
  window.location.href = '/';
}

// ── Auth ──────────────────────────────────────────────────────────────────────
export const login    = (data) => api.post('/api/auth/login', data);
export const register = (data) => api.post('/api/auth/register', data);

// ── Exams ─────────────────────────────────────────────────────────────────────
export const getExams      = ()           => api.get('/api/exams');
export const startExam     = (examId)     => api.get(`/api/exams/${examId}/start`);
export const submitExam    = (payload)    => api.post('/api/exams/submit', payload);
export const getMyResults  = ()           => api.get('/api/exams/my-results');

// ── Violations ────────────────────────────────────────────────────────────────
export const reportViolation = (examId, violationType) =>
  api.post('/api/violations', { examId, violationType });

export default api;
