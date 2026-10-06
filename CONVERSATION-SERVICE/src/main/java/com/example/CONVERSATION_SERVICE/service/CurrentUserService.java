package com.example.CONVERSATION_SERVICE.service;

import com.example.CONVERSATION_SERVICE.entity.CurrentUser;
import com.example.CONVERSATION_SERVICE.exception.AccessDeniedException;
import com.example.CONVERSATION_SERVICE.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CurrentUser getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal()
                instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Not authenticated");
        }

        return CurrentUser.builder()
                .id(principal.userId())
                .email(principal.email())
                .role(principal.role())
                .build();
    }

}