package com.example.CONVERSATION_SERVICE.security;

import com.example.CONVERSATION_SERVICE.dto.ApiResponse;
import com.example.CONVERSATION_SERVICE.utils.SecurityResponseUtil;
import com.example.CONVERSATION_SERVICE.utils.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final SecurityResponseUtil responseUtil;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ApiResponse<Object> apiResponse =
                ApiResponse.builder()
                        .success(false)
                        .message("Unauthorized")
                        .status(401)
                        .data(HttpStatus.UNAUTHORIZED.value())
                        .errors(List.of(authException.getMessage()))
                        .path(request.getRequestURI())
                        .traceId(TraceIdUtil.generate())
                        .timestamp(LocalDateTime.now())
                        .build();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );

        responseUtil.writeError(
                request,
                response,
                401,
                "Unauthorized!"
        );
    }
}