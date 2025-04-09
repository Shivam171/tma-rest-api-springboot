package com.project.task_management_app.controllers;

import com.project.task_management_app.exceptions.ResourceNotFoundException;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Request.CreateWorkspaceRequest;
import com.project.task_management_app.payload.Request.UpdateWorkspaceRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.WorkspaceResponse;
import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.repositories.WorkspaceRepository;
import com.project.task_management_app.services.UserDetailsImpl;
import com.project.task_management_app.services.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
@Tag(name = "Workspace Management", description = "APIs for managing workspaces (create, update, delete, get)")
public class WorkspaceController {
    private final WorkspaceService workspaceService;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new workspace", description = "Creates a private workspace by default for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Workspace created successfully")
    public ResponseEntity<APIResponse<WorkspaceResponse>> createWorkspace(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        APIResponse<WorkspaceResponse> response = workspaceService.createWorkspace(userDetails, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Update a workspace", description = "Update name, description or visibility (PRIVATE/PUBLIC) of a workspace")
    @ApiResponse(responseCode = "200", description = "Workspace updated successfully")
    public ResponseEntity<APIResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        APIResponse<WorkspaceResponse> response = workspaceService.updateWorkspace(workspaceId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Delete a workspace", description = "Delete a workspace by ID, default workspace cannot be deleted")
    @ApiResponse(responseCode = "200", description = "Workspace deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteWorkspace(
            @PathVariable UUID workspaceId
    ) {
        APIResponse<Void> response = workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all owned workspaces", description = "Fetch all workspaces owned by the currently logged-in user")
    @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkspaceResponse>>> getAllWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        APIResponse<List<WorkspaceResponse>> response = workspaceService.getAllWorkspaces(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Get a workspace by name", description = "Search and retrieve a workspace by its name")
    @ApiResponse(responseCode = "200", description = "Workspace found successfully")
    public ResponseEntity<APIResponse<WorkspaceResponse>> getWorkspaceByName(
            @RequestParam String name
    ) {
        APIResponse<WorkspaceResponse> response = workspaceService.getWorkspaceByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workspaceId}/invite/{entryCode}")
    @Operation(summary = "Join a workspace via invite link", description = "Shareable link to join a workspace")
    @ApiResponse(responseCode = "200", description = "Workspace joined successfully")
    public ResponseEntity<APIResponse<String>> joinWorkspaceByEntryCode(
            @PathVariable UUID workspaceId,
            @PathVariable String entryCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        return ResponseEntity.ok(workspaceService.joinWorkspaceByEntryCode(workspaceId, entryCode, userDetails.getId()));
    }
}
