package com.examportal.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ExamTerminatedException extends RuntimeException {
    public ExamTerminatedException(String message) { super(message); }
}
