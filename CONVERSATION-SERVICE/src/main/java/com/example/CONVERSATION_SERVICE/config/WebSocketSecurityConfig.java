package com.example.CONVERSATION_SERVICE.config;

import com.example.CONVERSATION_SERVICE.security.StompAuthorizationInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig
        implements WebSocketMessageBrokerConfigurer {

    private final StompAuthorizationInterceptor
            stompAuthorizationInterceptor;

    public WebSocketSecurityConfig(
            StompAuthorizationInterceptor
                    stompAuthorizationInterceptor
    ) {
        this.stompAuthorizationInterceptor =
                stompAuthorizationInterceptor;
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        registration.interceptors(
                stompAuthorizationInterceptor
        );
    }
}