package com.project.task_management_app.controllers;

import com.project.task_management_app.enums.Role;
import com.project.task_management_app.enums.WorkspaceType;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Request.SignInRequest;
import com.project.task_management_app.payload.Request.SignUpRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.JwtResponse;
import com.project.task_management_app.payload.Response.UserResponse;
import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.repositories.WorkspaceRepository;
import com.project.task_management_app.security.jwt.JwtUtils;
import com.project.task_management_app.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth Management", description = "APIs for auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkspaceRepository workspaceRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Authenticate a user and generate a JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<APIResponse<JwtResponse>> authenticateUser(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("USER");

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        );

        APIResponse<JwtResponse> response = new APIResponse<>(
                jwtResponse,
                "User authenticated successfully",
                true,
                HttpStatus.OK.value(),
                "POST",
                request.getRequestURI(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Username or email already exists")
    public ResponseEntity<APIResponse<UserResponse>> registerUser(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletRequest request) {

        // Check if username exists
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            APIResponse<UserResponse> errorResponse = new APIResponse<>(
                    null,
                    "Username is already taken!",
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "POST",
                    request.getRequestURI(),
                    LocalDateTime.now().toString()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Check if email exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            APIResponse<UserResponse> errorResponse = new APIResponse<>(
                    null,
                    "Email is already in use!",
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "POST",
                    request.getRequestURI(),
                    LocalDateTime.now().toString()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER); // New users are always USER
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        // Create default workspace "Home"
        Workspace homeWorkspace = new Workspace();
        homeWorkspace.setName("Home");
        homeWorkspace.setDescription("Your default workspace");
        homeWorkspace.setOwner(user);
        homeWorkspace.setMembers(Set.of(user));
        homeWorkspace.setEntryCode(UUID.randomUUID().toString().substring(0, 6));
        homeWorkspace.setType(WorkspaceType.DEFAULT);
        homeWorkspace.setCreatedAt(LocalDateTime.now());
        homeWorkspace.setUpdatedAt(LocalDateTime.now());

        workspaceRepository.save(homeWorkspace);

        // Create user response
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUserImgUrl(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        APIResponse<UserResponse> response = new APIResponse<>(
                userResponse,
                "User registered successfully!",
                true,
                HttpStatus.CREATED.value(),
                "POST",
                request.getRequestURI(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signout")
    @Operation(summary = "User logout", description = "Log out a user by clearing the security context")
    @ApiResponse(responseCode = "200", description = "User logged out successfully")
    public ResponseEntity<APIResponse<Void>> logoutUser(HttpServletRequest request) {
        SecurityContextHolder.clearContext(); // Clears authentication from the context

        APIResponse<Void> response = new APIResponse<>(
                null,
                "User logged out successfully!",
                true,
                HttpStatus.OK.value(),
                "POST",
                request.getRequestURI(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
