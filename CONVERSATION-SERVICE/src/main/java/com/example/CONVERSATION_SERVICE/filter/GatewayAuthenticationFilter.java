package com.example.CONVERSATION_SERVICE.filter;

import com.example.CONVERSATION_SERVICE.security.UserPrincipal;
import com.example.CONVERSATION_SERVICE.utils.GatewayHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException,
            IOException {

        String userId = request.getHeader(GatewayHeaders.USER_ID);

        String email = request.getHeader(GatewayHeaders.USER_EMAIL);

        String role = request.getHeader(GatewayHeaders.USER_ROLE);

        if (userId != null && email != null && role != null) {

            UserPrincipal principal = UserPrincipal.builder()
                    .userId(Long.parseLong(userId))
                    .email(email)
                    .role(role)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug(
                    "Authenticated {}",
                    email
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}