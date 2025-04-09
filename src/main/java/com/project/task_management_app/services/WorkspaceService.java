package com.project.task_management_app.services;

import com.project.task_management_app.enums.WorkspaceType;
import com.project.task_management_app.exceptions.InvalidRequestException;
import com.project.task_management_app.exceptions.ResourceNotFoundException;
import com.project.task_management_app.mapper.WorkspaceMapper;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Request.CreateWorkspaceRequest;
import com.project.task_management_app.payload.Request.UpdateWorkspaceRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.WorkspaceResponse;
import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.repositories.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.project.task_management_app.mapper.WorkspaceMapper.mapToWorkspaceResponse;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    @Autowired
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final EmailService emailService;

    public APIResponse<WorkspaceResponse> createWorkspace(UserDetailsImpl userDetails, CreateWorkspaceRequest request) {
        User owner = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (workspaceRepository.existsByNameIgnoreCaseAndOwner(request.getName(), owner)) {
            throw new InvalidRequestException("Workspace name: "+ request.getName() +  ", already exists for the current user.");
        }

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setOwner(owner);
        workspace.setMembers(Set.of(owner));
        workspace.setEntryCode(UUID.randomUUID().toString().substring(0, 6));
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setType(WorkspaceType.PRIVATE);

        Workspace saved = workspaceRepository.save(workspace);

        APIResponse<WorkspaceResponse> response = new APIResponse<>();
        response.setData(mapToWorkspaceResponse(saved));
        response.setSuccess(true);
        response.setStatusCode(201);
        response.setMethod("POST");
        response.setMessage("Workspace created successfully");
        response.setPath("/api/v1/workspaces");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
}

    public APIResponse<WorkspaceResponse> updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        if (request.getName() != null) workspace.setName(request.getName());

        if (request.getDescription() != null) workspace.setDescription(request.getDescription());

        if (request.getType() != null) {
            workspace.setType(request.getType());
        } else {
            // If not explicitly set, auto-switch to PUBLIC if more than 1 member
            if (workspace.getMembers().size() > 1 && workspace.getType() == WorkspaceType.PRIVATE) {
                workspace.setType(WorkspaceType.PUBLIC);
            }
        }

        workspace.setUpdatedAt(LocalDateTime.now());
        Workspace updated = workspaceRepository.save(workspace);

        return new APIResponse<>(
                mapToWorkspaceResponse(updated),
                "Workspace updated successfully",
                true,
                200,
                "PUT",
                "/api/v1/workspaces/" + id,
                String.valueOf(LocalDateTime.now())
        );
    }

    public APIResponse<Void> deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: "+ workspaceId));

        if (workspace.getType() == WorkspaceType.DEFAULT ) {
            throw new RuntimeException("Cannot delete default workspace");
        }

        workspaceRepository.delete(workspace);

        return new APIResponse<>(
                null,
                "Workspace deleted successfully",
                true,
                200,
                "DELETE",
                "/api/v1/workspaces/" + workspaceId,
                String.valueOf(LocalDateTime.now())
        );
    }

    public APIResponse<WorkspaceResponse> getWorkspaceByName(String name) {
        Workspace workspace = workspaceRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with name: " + name));

        return new APIResponse<>(
                mapToWorkspaceResponse(workspace),
                "Workspace found successfully",
                true,
                200,
                "GET",
                "/api/v1/workspaces/" + workspace.getId(),
                String.valueOf(LocalDateTime.now())
        );
    }

    public APIResponse<List<WorkspaceResponse>> getAllWorkspaces(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<WorkspaceResponse> workspaceResponses = workspaceRepository.findByOwner(user)
                .stream()
                .map(WorkspaceMapper::mapToWorkspaceResponse)
                .collect(Collectors.toList());

        return new APIResponse<>(
                workspaceResponses,
                "Workspaces found successfully",
                true,
                200,
                "GET",
                "/api/v1/workspaces",
                String.valueOf(LocalDateTime.now())
        );
    }

    public APIResponse<String> joinWorkspaceByEntryCode(UUID workspaceId, String entryCode, UUID userId) {
        Workspace workspace = workspaceRepository.findByIdAndEntryCode(workspaceId, entryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite link"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found, Please signup first"));

        if (workspace.getMembers().contains(user)) {
            return new APIResponse<>(
                    null,
                    "Already a member of the workspace",
                    true,
                    200,
                    "POST",
                    "/api/v1/invite/" + entryCode,
                    String.valueOf(LocalDateTime.now())
            );
        }

        workspace.getMembers().add(user);
        workspaceRepository.save(workspace);

        String workspaceLink = "https://taskbuddy.com/workspace/" + workspace.getId();
        emailService.sendWorkspaceWelcomeEmail(
                user.getEmail(),
                user.getUsername(),
                workspace.getName(),
                workspaceLink
        );

        return new APIResponse<>(
                null,
                "Workspace joined successfully",
                true,
                200,
                "POST",
                "/api/v1/invite/" + entryCode,
                String.valueOf(LocalDateTime.now())
        );
    }
}

