package com.project.task_management_app.services;

import com.project.task_management_app.enums.AssignmentStatus;
import com.project.task_management_app.exceptions.ResourceNotFoundException;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.TaskAssignment;
import com.project.task_management_app.repositories.TaskAssignmentRepository;
import com.project.task_management_app.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public void updateAssignmentStatus(UUID assignmentId, AssignmentStatus status) {
        TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        assignment.setStatus(status);
        taskAssignmentRepository.save(assignment);

        Task task = assignment.getTask();
        taskService.updateGlobalStatus(task);
    }
}
