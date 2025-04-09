package com.project.task_management_app.payload.Response;

import com.project.task_management_app.enums.WorkspaceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class WorkspaceResponse {
    private final UUID id;
    private final String name;
    private final String description;
    private final String entryCode;
    private final UserResponse owner;
    private final Set<UserResponse> members;
    private final WorkspaceType type;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String inviteLink;
}
