package com.example.CONVERSATION_SERVICE;

import com.example.CONVERSATION_SERVICE.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@ConfigurationPropertiesScan
@EnableFeignClients
//@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class ConversationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConversationServiceApplication.class, args);
	}

}
