package com.example.CONVERSATION_SERVICE.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    private Access access = new Access();

    @Getter
    @Setter
    public static class Access {

        private long expiration;
    }
}