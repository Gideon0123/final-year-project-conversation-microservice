package com.example.CONVERSATION_SERVICE.config;

import com.example.CONVERSATION_SERVICE.security.UserPrincipal;
import com.example.CONVERSATION_SERVICE.utils.GatewayHeaders;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {

        return template -> {

            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();

            if (authentication == null ||
                    !(authentication.getPrincipal()
                            instanceof UserPrincipal principal)) {
                return;
            }

            template.header(
                    GatewayHeaders.USER_ID,
                    principal.userId().toString()
            );

            template.header(
                    GatewayHeaders.USER_EMAIL,
                    principal.email()
            );

            template.header(
                    GatewayHeaders.USER_ROLE,
                    principal.role()
            );
        };

    }
}