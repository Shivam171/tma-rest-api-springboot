package com.project.task_management_app.controllers;

import com.project.task_management_app.models.User;
import com.project.task_management_app.payload.Request.UpdateUserRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.UserResponse;
import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "APIs for managing users (No Admin specific APIs added yet).")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Get the currently logged-in user's details
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the details of the currently logged-in user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user details")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<APIResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponse userResponse = new UserResponse(
                userDetails.getId(),
                userDetails.getUserImgUrl(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getCreatedAt(),
                userDetails.getUpdatedAt()
        );

        APIResponse<UserResponse> response = new APIResponse<>(
                userResponse,
                "User details retrieved successfully",
                true,
                HttpStatus.OK.value(),
                "GET",
                "/api/v1/users/me",
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Update the logged-in user's profile (username, email, etc.)
    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Update the profile of the currently logged-in user")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<APIResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateUserRequest updateRequest,
            HttpServletRequest request
    ) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update fields only if they are provided
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken!");
            }
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getUserImgUrl() != null) {
            user.setUserImgUrl(updateRequest.getUserImgUrl());
        }

        if (updateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse(
                updatedUser.getId(),
                updatedUser.getUserImgUrl(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt()
        );

        APIResponse<UserResponse> response = new APIResponse<>(
                userResponse,
                "User updated successfully",
                true,
                HttpStatus.OK.value(),
                "PUT",
                request.getRequestURI(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
