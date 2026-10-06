package com.example.CONVERSATION_SERVICE.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService
    ) {
        this.jwtService =
                jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token =
                resolveToken(request);

        if (token != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            try {

                if (jwtService.validateAccessToken(token)) {

                    AuthenticatedUser user =
                            jwtService
                                    .buildAuthenticatedUser(
                                            token
                                    );

                    String role =
                            user.role();

                    if (role != null
                            && !role.isBlank()) {

                        UsernamePasswordAuthenticationToken
                                authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        List.of(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_" + role
                                                )
                                        )
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(
                                        authentication
                                );
                    }
                }

            } catch (Exception ex) {

                SecurityContextHolder
                        .clearContext();
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String resolveToken(
            HttpServletRequest request
    ) {

        String authorization =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            return authorization.substring(7);
        }

        return resolveAccessTokenCookie(
                request
        );
    }

    private String resolveAccessTokenCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("accessToken".equals(
                    cookie.getName()
            )) {

                return cookie.getValue();
            }
        }

        return null;
    }
}