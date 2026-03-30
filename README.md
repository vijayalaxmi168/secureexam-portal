# 🛡️ SecureExam Portal

A full-stack, high-security Online Exam Portal built with **Java 21 + Spring Boot 3.4** (backend) and **React 19** (frontend).

---

## ✅ Features

### Backend
| Feature | Detail |
|---|---|
| Stateless JWT Auth | Access token (15 min) + Refresh token (7 days) with rotation |
| `jti` claim | Every access token has a unique UUID — auditable |
| DTO Projection | `QuestionResponse` never includes `correctOption` |
| Anti-Cheat Engine | 3 violations → exam force-terminated at service layer |
| Global Exception Handler | `@RestControllerAdvice` — consistent JSON errors |
| Role-based Access | `ROLE_ADMIN` / `ROLE_STUDENT` via `@PreAuthorize` |
| Swagger UI | Full API docs at `http://localhost:8080/swagger-ui.html` |
| Data Seeder | Auto-creates admin, student, and sample exam on startup |

### Frontend
| Feature | Detail |
|---|---|
| Axios Interceptor | Auto-attaches JWT; silently refreshes on 401 |
| Tab-switch detection | `visibilitychange` → POST `/api/violations` |
| Auto-submit | Timer hits 0 → submits with reason `TIMER_EXPIRED` |
| Force-terminate | 3rd violation → submits with `FORCE_TERMINATED` |
| Right-click disabled | `contextmenu` blocked globally |
| Copy/Paste disabled | `copy`, `paste`, `cut` blocked globally |
| Dark Proctor UI | Custom CSS, no heavy UI library |

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+
- Node.js 18+

---

### 1. Database Setup

```sql
CREATE DATABASE exam_portal;
```

Update `backend/src/main/resources/application.properties` if needed:
```properties
spring.datasource.username=root
spring.datasource.password=root
```

---

### 2. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

On startup, the `DataSeeder` automatically creates:
- **Admin:**   `admin` / `admin123`
- **Student:** `student1` / `student123`
- **Sample Exam:** "Java Fundamentals — Midterm" with 5 MCQ questions

Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### 3. Run the Frontend

```bash
cd frontend
npm install
npm start
```

Frontend runs at: **http://localhost:3000**

---

## 📁 Project Structure

```
exam-portal/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/examportal/
│       ├── config/          SecurityConfig, OpenApiConfig, DataSeeder
│       ├── controller/      AuthController, ExamController, ViolationController
│       ├── dto/             13 clean DTOs (QuestionResponse has NO correctOption)
│       ├── entity/          User, Exam, Question, ExamResult, ViolationAudit, RefreshToken
│       ├── exception/       GlobalExceptionHandler + 3 custom exceptions
│       ├── filter/          JwtFilter (OncePerRequestFilter)
│       ├── repository/      5 JPA repositories
│       ├── security/        JwtUtil, CustomUserDetailsService
│       └── service/         AuthService, RefreshTokenService, ExamService
└── frontend/
    └── src/
        ├── services/api.js  Axios + JWT interceptor + auto-refresh
        ├── pages/           LoginPage, ExamListPage, ExamRoomPage, ResultPage, MyResultsPage
        ├── App.js           State-machine router (no react-router)
        └── index.css        Full dark proctor theme
```

---

## 🔑 API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register student |
| POST | `/api/auth/login` | Public | Login → tokens |
| POST | `/api/auth/refresh` | Public | Rotate refresh token |
| GET  | `/api/exams` | Student | List active exams |
| GET  | `/api/exams/{id}/start` | Student | Get questions (no answers) |
| POST | `/api/exams/submit` | Student | Submit answers |
| GET  | `/api/exams/my-results` | Student | View own results |
| POST | `/api/exams` | Admin | Create exam |
| POST | `/api/exams/questions` | Admin | Add question |
| POST | `/api/violations` | Student | Report violation |

---

## 🎤 Interview Talking Points

1. **Why DTOs?** — The `Question` entity has `correctOption` but `QuestionResponse` does not. Even if the HTTP response is intercepted, answers are never leaked.

2. **Why stateless JWT?** — No server-side session storage. Every request is self-contained. Scales horizontally without sticky sessions.

3. **How does anti-cheat work?** — `ViolationAudit` records every violation in the DB. The `ExamService` calls `enforceViolationLimit()` before loading questions AND before accepting submissions. A terminated student cannot do either.

4. **Refresh token rotation?** — On every `/api/auth/refresh` call, the old refresh token is deleted and a new one is issued. Replaying a stolen token fails because it no longer exists in the DB.

5. **Why `jti` in the JWT?** — Each access token gets a unique UUID. In a more advanced setup, you could maintain a revocation list of jtis to invalidate specific tokens before they expire.

6. **`@RestControllerAdvice`** — Centralises all exception → HTTP response mapping. No controller ever writes error JSON manually, ensuring consistency.
