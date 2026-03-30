# 🛡️ SecureExam Portal

SecureExam Portal
A full-stack online examination portal where students can take proctored exams securely. The system is built to prevent cheating at every level — correct answers never leave the server, tab switching is tracked in real time, and three violations automatically terminate the exam session.

What This Project Does
Students log in and see a list of available exams. When they start an exam, questions are loaded from the server but the correct answers are never included in the response — they only exist in the database. A 30-minute countdown timer runs during the exam and automatically submits when time runs out. If a student switches tabs, a violation is recorded in the database. After three violations the exam is force-terminated and submitted automatically.
Admins can create exams and add questions through protected API endpoints. All admin routes are blocked for students at the backend level.

Tech Stack

Backend — Java 21, Spring Boot 3.4, Spring Security 6, Spring Data JPA
Database — MySQL
Security — JWT with Refresh Token Rotation
Frontend — React 19, Axios
API Docs — Swagger UI


Security Features

Correct answers are never sent to the client — enforced by the DTO layer
JWT access tokens expire in 15 minutes
Refresh tokens are single use — old token is deleted on every refresh
Passwords are hashed using BCrypt with strength 12
Anti-cheat violations are stored in the database and enforced on the server — not just JavaScript
Students cannot submit an exam twice


How to Run This Project
Requirements

Java 21
Maven
MySQL 8
Node.js

Step 1 — Create the Database
sqlCREATE DATABASE exam_portal;
```

### Step 2 — Configure Database Password

Open `backend/src/main/resources/application.properties` and update:
```
spring.datasource.password=your_mysql_password
```

### Step 3 — Run the Backend
```
cd backend
mvn spring-boot:run
```

The app will auto-create all tables and seed the following accounts on first run:

| Username | Password | Role |
|---|---|---|
| admin | admin123 | Admin |
| student1 | student123 | Student |

### Step 4 — Run the Frontend
```
cd frontend
npm install
npm start
```

Open **http://localhost:3000** in your browser.

### API Documentation
```
http://localhost:8080/swagger-ui.html

API Endpoints
MethodEndpointAccessDescriptionPOST/api/auth/registerPublicRegister new studentPOST/api/auth/loginPublicLogin and get tokensPOST/api/auth/refreshPublicRefresh access tokenGET/api/examsStudentList all active examsGET/api/exams/{id}/startStudentStart exam — no correct answers includedPOST/api/exams/submitStudentSubmit answers and get scoreGET/api/exams/my-resultsStudentView past resultsPOST/api/examsAdminCreate a new examPOST/api/exams/questionsAdminAdd question to examPOST/api/violationsStudentRecord anti-cheat violation

How the Anti-Cheat Works
Every time a student switches to another tab, the browser sends a request to the violations endpoint. The backend saves this event with a timestamp. When the count reaches three, the backend returns a terminated flag. The frontend immediately submits the exam with the reason FORCE_TERMINATED. Even if someone disables JavaScript, the backend will still reject any further submissions from a terminated session.

How Scoring Works
The student sends their answers as a map of question ID to their chosen option. The server fetches the correct answers from the database and compares them independently. The client has no influence over the score — there is no way to manipulate it from the browser.
