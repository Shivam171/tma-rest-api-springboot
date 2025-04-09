package com.project.task_management_app.mapper;

import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Response.UserResponse;
import com.project.task_management_app.payload.Response.WorkspaceResponse;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;
import java.util.stream.Collectors;

public class WorkspaceMapper {

    @Value("${app.workspace.invite.base-url}")
    private static String inviteBaseUrl;

    private WorkspaceMapper() {}

    public static WorkspaceResponse mapToWorkspaceResponse(Workspace workspace) {
        String inviteLink = inviteBaseUrl + "/invite/" + workspace.getEntryCode();
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getEntryCode(),
                mapToUserResponse(workspace.getOwner()),
                mapToUserResponseSet(workspace.getMembers()),
                workspace.getType(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt(),
                inviteLink
        );
    }

    private static UserResponse mapToUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUserImgUrl(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private static Set<UserResponse> mapToUserResponseSet(Set<User> users) {
        if (users == null) return null;
        return users.stream()
                .map(WorkspaceMapper::mapToUserResponse)
                .collect(Collectors.toSet());
    }
}
