package com.examportal.config;

import com.examportal.entity.*;
import com.examportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder — runs once on application startup.
 *
 * Seeds:
 *  - admin   / admin123   (ROLE_ADMIN)
 *  - student1 / student123 (ROLE_STUDENT)
 *  - 1 sample exam "Java Fundamentals" with 5 MCQ questions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository     userRepository;
    private final ExamRepository     examRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedExam();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@examportal.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ROLE_ADMIN)
                    .build());
            log.info("✅  Seeded  →  admin / admin123  (ROLE_ADMIN)");
        }
        if (!userRepository.existsByUsername("student1")) {
            userRepository.save(User.builder()
                    .username("student1")
                    .email("student1@examportal.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(User.Role.ROLE_STUDENT)
                    .build());
            log.info("✅  Seeded  →  student1 / student123  (ROLE_STUDENT)");
        }
    }

    private void seedExam() {
        if (examRepository.count() > 0) return;

        Exam exam = examRepository.save(Exam.builder()
                .title("Java Fundamentals — Midterm")
                .description("Covers OOP, Collections, Exceptions, and Core Java concepts.")
                .durationMinutes(30)
                .totalMarks(25)
                .passMarks(15)
                .isActive(true)
                .build());

        Object[][] questions = {
            {"Which keyword prevents method overriding in Java?",
             "static", "private", "final", "abstract", "C", 5},
            {"Which collection maintains insertion order and allows duplicates?",
             "HashSet", "TreeSet", "LinkedList", "HashMap", "C", 5},
            {"What is the parent class of all Java classes?",
             "Class", "Object", "Base", "Super", "B", 5},
            {"Which exception is thrown when dividing an integer by zero?",
             "NullPointerException", "ArithmeticException", "IllegalArgumentException", "RuntimeException", "B", 5},
            {"Which access modifier makes a member visible only within the same class?",
             "protected", "default", "public", "private", "D", 5},
        };

        for (Object[] q : questions) {
            questionRepository.save(Question.builder()
                    .exam(exam)
                    .questionText((String) q[0])
                    .optionA((String) q[1])
                    .optionB((String) q[2])
                    .optionC((String) q[3])
                    .optionD((String) q[4])
                    .correctOption((String) q[5])
                    .marks((Integer) q[6])
                    .build());
        }

        log.info("✅  Seeded  →  Exam '{}' with {} questions", exam.getTitle(), questions.length);
    }
}
