package com.project.task_management_app.payload.Response;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TaskResponse{
    private final UUID id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final String category;
    private final String attachmentUrl;
    private final LocalDateTime dueDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final UUID workspaceId;
    private final UserResponse createdBy;
    private final Set<TaskAssigneeResponse> assignees;
}
