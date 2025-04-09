package com.project.task_management_app.payload.Request;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private String category;

    private String attachmentUrl;

    @NotNull(message = "Due date cannot be null")
    private LocalDateTime dueDate;

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    private Set<UUID> assigneeIds; // Optional list of user IDs to assign the task to
}