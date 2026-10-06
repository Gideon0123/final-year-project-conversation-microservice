package com.example.CONVERSATION_SERVICE.exception;

import com.example.CONVERSATION_SERVICE.dto.ApiResponse;
import com.example.CONVERSATION_SERVICE.utils.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), request);
    }

    @ExceptionHandler(UnauthorizedConversationAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedConversationAccess(
            UnauthorizedConversationAccessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), request);
    }

    @ExceptionHandler(CollaborationRequiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleCollaborationRequired(
            CollaborationRequiredException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), request);
    }
//
//    @ExceptionHandler(MissingIdempotencyKeyException.class)
//    public ResponseEntity<ApiResponse<Object>> handleMissingIdempotencyKey(
//            MissingIdempotencyKeyException ex,
//            HttpServletRequest request
//    ) {
//        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request);
//    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedReview(
            InvalidTokenException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request);
    }


//    @ExceptionHandler(ResearchPaperNotFoundException.class)
//    public ResponseEntity<ApiResponse<Object>> handleResearchNotFound(
//            ResearchPaperNotFoundException ex,
//            HttpServletRequest request
//    ) {
//        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request);
//    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOperation(
            InvalidOperationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request);
    }

//    @ExceptionHandler(FileStorageException.class)
//    public ResponseEntity<ApiResponse<Object>> handleFileStorageException(
//            FileStorageException ex,
//            HttpServletRequest request
//    ) {
//        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .data(errors)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), request);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            String message,
            int status,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(
                ApiResponse.builder()
                        .success(false)
                        .message(message)
                        .status(status)
                        .data(null)
                        .errors(List.of(message))
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
