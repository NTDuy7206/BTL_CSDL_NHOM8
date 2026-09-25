package com.ntd.csdl.config;

import com.ntd.task.exception.TasksException;
import com.ntd.task.exception.UsersException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public  ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return   ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(UsersException.class)
    public ResponseEntity<String> handleUserException(UsersException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(TasksException.class)
    public ResponseEntity<String> handleUserException(TasksException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public  ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return  ResponseEntity.badRequest().body(ex.getMessage());
    }

}
