package com.example.CONVERSATION_SERVICE.feign;

import com.example.CONVERSATION_SERVICE.config.FeignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "COLLABORATION-SERVICE",
        configuration = FeignConfig.class
)
public interface CollaborationClient {

    @GetMapping(
            "/internal/collaboration/connections/check"
    )
    boolean areConnected(
            @RequestParam("userId") Long userId,
            @RequestParam("otherUserId") Long otherUserId
    );
}