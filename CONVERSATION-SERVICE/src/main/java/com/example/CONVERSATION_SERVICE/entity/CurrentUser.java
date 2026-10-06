package com.example.CONVERSATION_SERVICE.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {

    private Long id;
    private String email;
    private String role;

}