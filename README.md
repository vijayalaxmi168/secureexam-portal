# 🛡️ SecureExam Portal

> A JWT-secured, role-based online examination system with real-time anti-cheat proctoring.

A full-stack proctored exam portal where correct answers never leave the server, tab switching is tracked in real time, and three violations automatically terminate the exam session — enforced at the backend service layer, not just JavaScript.

---

## ✨ Features

### 🔐 Security
- Correct answers are **never sent to the client** — enforced by DTO projection
- JWT access tokens expire in **15 minutes**
- Refresh tokens are **single use** — old token deleted on every refresh
- Passwords hashed with **BCrypt strength 12**
- Students **cannot submit an exam twice**

### 🎯 Anti-Cheat
- Tab switching is detected via `visibilitychange` event
- Every violation is recorded in the database with timestamp
- **3 violations = exam force-terminated** at the service layer
- Enforced on the server — cannot be bypassed by disabling JavaScript

### 👥 Role Based Access
- `ROLE_ADMIN` → create exams, add questions
- `ROLE_STUDENT` → take exams, view results
- All routes protected at backend level via Spring Security

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.4 |
| Security | Spring Security 6, JWT, BCrypt |
| Database | MySQL 8, Spring Data JPA, Hibernate |
| Frontend | React 19, Axios |
| API Docs | Swagger UI / SpringDoc OpenAPI |

---

## 📡 API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new student |
| POST | `/api/auth/login` | Public | Login and get tokens |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| GET | `/api/exams` | Student | List all active exams |
| GET | `/api/exams/{id}/start` | Student | Start exam — no correct answers |
| POST | `/api/exams/submit` | Student | Submit answers and get score |
| GET | `/api/exams/my-results` | Student | View past results |
| POST | `/api/exams` | Admin | Create a new exam |
| POST | `/api/exams/questions` | Admin | Add question to exam |
| POST | `/api/violations` | Student | Record anti-cheat violation |

---

## 🔒 How Security Works

### Correct Answers Never Leave the Server
The `Question` entity has a `correctOption` field in the database.
The `QuestionResponse` DTO intentionally excludes it.
Even if someone intercepts the HTTP response — they will never find the answer.

### Anti-Cheat Flow
```
Student switches tab
        ↓
Frontend fires visibilitychange event
        ↓
POST /api/violations → saved to violation_audits table
        ↓
Backend counts violations for this user + exam
        ↓
Count >= 3 → terminated = true
        ↓
Frontend force-submits with reason FORCE_TERMINATED
        ↓
Backend rejects any further access to this exam
```

### Scoring Flow
```
Student submits { questionId → "A", "B", "C", "D" }
        ↓
Backend fetches correctOption from DATABASE
        ↓
Compares each answer server-side
        ↓
Score calculated — client has zero influence
        ↓
Result saved with passed/failed status
```

### Token Rotation
```
Login → Access Token (15 min) + Refresh Token (7 days)
        ↓
Access token expires → call /api/auth/refresh
        ↓
Old refresh token DELETED from database
New refresh token ISSUED
        ↓
Stolen token replayed → rejected (no longer exists)
```

---

## 📁 Project Structure
```
secureexam-portal/
├── backend/
│   └── src/main/java/com/examportal/
│       ├── config/       → SecurityConfig, DataSeeder, OpenApiConfig
│       ├── controller/   → AuthController, ExamController, ViolationController
│       ├── dto/          → 13 DTOs (QuestionResponse has NO correctOption)
│       ├── entity/       → User, Exam, Question, ExamResult, ViolationAudit, RefreshToken
│       ├── exception/    → GlobalExceptionHandler + custom exceptions
│       ├── filter/       → JwtFilter
│       ├── repository/   → 5 JPA repositories
│       ├── security/     → JwtUtil, CustomUserDetailsService
│       └── service/      → AuthService, ExamService, RefreshTokenService
└── frontend/
    └── src/
        ├── services/     → api.js (Axios + JWT interceptor + auto-refresh)
        ├── pages/        → LoginPage, ExamListPage, ExamRoomPage, ResultPage, MyResultsPage
        ├── App.js        → State-machine router
        └── index.css     → Dark proctor theme
```

---
