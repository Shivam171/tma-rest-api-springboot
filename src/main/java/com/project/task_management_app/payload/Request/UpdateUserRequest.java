package com.project.task_management_app.payload.Request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateUserRequest {
    private String username;
    @Email
    private String email;
    private String password;
    private String userImgUrl;
    private LocalDateTime updatedAt;
}