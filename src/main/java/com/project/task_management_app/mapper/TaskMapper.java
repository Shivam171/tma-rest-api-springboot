package com.project.task_management_app.mapper;

import com.project.task_management_app.enums.AssignmentStatus;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.TaskAssignment;
import com.project.task_management_app.models.User;
import com.project.task_management_app.payload.Response.TaskAssigneeResponse;
import com.project.task_management_app.payload.Response.TaskResponse;
import com.project.task_management_app.payload.Response.UserResponse;

import java.util.*;
import java.util.stream.Collectors;

public class TaskMapper {

    private TaskMapper() {} // Prevent instantiation

    public static TaskResponse mapToTaskResponse(Task task) {
        // Get assignments directly from the repository or fetch them via task
        List<TaskAssignment> assignments = task.getAssignments();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCategory(),
                task.getAttachmentUrl(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getWorkspace() != null ? task.getWorkspace().getId() : null,
                mapToUserResponse(task.getUser()),
                mapToAssigneeResponseSet(task.getAssignees(), assignments)
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

    private static Set<TaskAssigneeResponse> mapToAssigneeResponseSet(Set<User> assignees, List<TaskAssignment> assignments) {
        if (assignees == null || assignees.isEmpty()) return Collections.emptySet();

        // Create a map of user ID to assignment for quick lookup
        Map<UUID, TaskAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getAssignee().getId(),
                        assignment -> assignment
                ));

        return assignees.stream()
                .map(user -> {
                    TaskAssignment assignment = assignmentMap.get(user.getId());
                    return new TaskAssigneeResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            assignment != null ? assignment.getStatus() : AssignmentStatus.PENDING,
                            assignment != null ? assignment.getAssignedAt() : null
                    );
                })
                .collect(Collectors.toSet());
    }
}