package br.com.medicacao.saas.api;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantAuthenticationException;
import br.com.medicacao.saas.organization.OrganizationConflictException;
import br.com.medicacao.saas.organization.OrganizationNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid fields.");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage,
                                Collectors.toList()))));
        return problem;
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    ProblemDetail handleNotFound(OrganizationNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
    }

    @ExceptionHandler(OrganizationConflictException.class)
    ProblemDetail handleConflict(OrganizationConflictException exception) {
        return problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage());
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    ProblemDetail handleAccessDenied(TenantAccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", exception.getMessage());
    }

    @ExceptionHandler(TenantAuthenticationException.class)
    ProblemDetail handleAuthentication(TenantAuthenticationException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication required", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
