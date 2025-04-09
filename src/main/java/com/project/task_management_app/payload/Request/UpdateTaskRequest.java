package com.project.task_management_app.payload.Request;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
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
public class UpdateTaskRequest {
    private String title;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private String category;

    private String attachmentUrl;

    private LocalDateTime dueDate;

    private Set<UUID> assigneeIds;
}
