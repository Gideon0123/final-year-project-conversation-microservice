package com.example.CONVERSATION_SERVICE.security;

import com.example.CONVERSATION_SERVICE.dto.ApiResponse;
import com.example.CONVERSATION_SERVICE.utils.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        ApiResponse<Object> apiResponse =
                ApiResponse.builder()
                        .success(false)
                        .message("Access denied")
                        .status(HttpStatus.FORBIDDEN.value())
                        .data(null)
                        .errors(List.of(ex.getMessage()))
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(LocalDateTime.now())
                        .build();

        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );
    }

}