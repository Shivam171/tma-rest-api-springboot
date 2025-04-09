package com.project.task_management_app.payload.Response;

import com.project.task_management_app.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TaskAssigneeResponse {
    private UUID id;
    private String name;
    private String email;
    private AssignmentStatus status;
    private LocalDateTime assignedAt;
}