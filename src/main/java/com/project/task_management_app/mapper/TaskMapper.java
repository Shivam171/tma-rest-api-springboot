package com.project.task_management_app.mapper;

import com.project.task_management_app.models.Task;
import com.project.task_management_app.payload.Response.TaskResponse;

public class TaskMapper {

    private TaskMapper() {} // Prevent instantiation

    public static TaskResponse mapToTaskResponse(Task task) {
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
                task.getUpdatedAt()
        );
    }
}