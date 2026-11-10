package br.com.medicacao.saas.api;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import br.com.medicacao.saas.config.RequestCorrelationFilter;
import br.com.medicacao.saas.catalog.MedicationConflictException;
import br.com.medicacao.saas.catalog.MedicationNotFoundException;
import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantAuthenticationException;
import br.com.medicacao.saas.organization.OrganizationConflictException;
import br.com.medicacao.saas.organization.OrganizationNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        return handleValidation(exception, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "The request contains invalid fields.");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage,
                                Collectors.toList()))));
        addCorrelationId(problem, request);
        return problem;
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    ProblemDetail handleNotFound(OrganizationNotFoundException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    ProblemDetail handleMedicationNotFound(MedicationNotFoundException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler(OrganizationConflictException.class)
    ProblemDetail handleConflict(OrganizationConflictException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request);
    }

    @ExceptionHandler(MedicationConflictException.class)
    ProblemDetail handleMedicationConflict(MedicationConflictException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Concurrent modification",
                "The resource was modified by another request.", request);
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    ProblemDetail handleAccessDenied(TenantAccessDeniedException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", exception.getMessage(), request);
    }

    @ExceptionHandler(TenantAuthenticationException.class)
    ProblemDetail handleAuthentication(TenantAuthenticationException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication required", exception.getMessage(),
                request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        addCorrelationId(problem, request);
        return problem;
    }

    private void addCorrelationId(ProblemDetail problem, HttpServletRequest request) {
        if (request != null) {
            String correlationId = request.getHeader(RequestCorrelationFilter.CORRELATION_HEADER);
            if (correlationId != null && !correlationId.isBlank()) {
                problem.setProperty("correlationId", correlationId);
            }
        }
    }
}
