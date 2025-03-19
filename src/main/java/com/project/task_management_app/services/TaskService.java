package com.project.task_management_app.services;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import com.project.task_management_app.exceptions.AccessDeniedException;
import com.project.task_management_app.exceptions.InvalidRequestException;
import com.project.task_management_app.exceptions.ResourceNotFoundException;
import com.project.task_management_app.exceptions.TaskAlreadyExistsException;
import com.project.task_management_app.mapper.TaskMapper;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.User;
import com.project.task_management_app.payload.Request.CreateTaskRequest;
import com.project.task_management_app.payload.Request.UpdateTaskRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.TaskResponse;
import com.project.task_management_app.repositories.TaskRepository;

import com.project.task_management_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.project.task_management_app.mapper.TaskMapper.mapToTaskResponse;

@Service
@RequiredArgsConstructor
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final UserRepository userRepository;

    // Get all tasks
    public APIResponse<List<TaskResponse>> getAllTasks(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(sortDirection, sortBy));

        Page<Task> tasksPage = taskRepository.findAll(pageable);

        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();

        APIResponse<List<TaskResponse>> response = new APIResponse<>();

        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks retrieved successfully");
        response.setPath("/api/v1/tasks");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }


    // Get task by id
    public APIResponse<TaskResponse> getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(task));
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Task retrieved successfully");
        response.setPath("/api/v1/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Create task
    public APIResponse<TaskResponse> createTask(UserDetailsImpl userDetails, CreateTaskRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        // Check if task with same title already exists
        Optional<Task> existingTask = taskRepository.findByTitle(request.getTitle());
        if (existingTask.isPresent()) {
            throw new TaskAlreadyExistsException("Task with title '" + request.getTitle() + "' already exists");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setCategory(request.getCategory());
        task.setAttachmentUrl(request.getAttachmentUrl());
        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(savedTask));
        response.setSuccess(true);
        response.setStatusCode(201);
        response.setMethod("POST");
        response.setMessage("Task created successfully");
        response.setPath("/api/v1/tasks");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Update task
    public APIResponse<TaskResponse> updateTask(UserDetailsImpl userDetails, UUID taskId, UpdateTaskRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Ensure that the logged-in user is the task owner
        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this task");
        }

        // Check if title is being changed and if new title already exists
        if (request.getTitle() != null && !task.getTitle().equals(request.getTitle())) {
            Optional<Task> taskWithSameTitle = taskRepository.findByTitle(request.getTitle());
            if (taskWithSameTitle.isPresent() && !taskWithSameTitle.get().getId().equals(taskId)) {
                throw new TaskAlreadyExistsException("Another task with title '" + request.getTitle() + "' already exists");
            }
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getCategory() != null) {
            task.setCategory(request.getCategory());
        }

        if (request.getAttachmentUrl() != null) {
            task.setAttachmentUrl(request.getAttachmentUrl());
        }

        if(request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        Task updatedTask = taskRepository.save(task);

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(updatedTask));
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("PUT");
        response.setMessage("Task updated successfully");
        response.setPath("/api/v1/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Delete task
    public APIResponse<Void> deleteTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);

        APIResponse<Void> response = new APIResponse<>();
        response.setData(null);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("DELETE");
        response.setMessage("Task deleted successfully");
        response.setPath("/api/v1/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Search Tasks
    public APIResponse<List<TaskResponse>> searchTasks(String title, String status, String priority,
                                                       String category, LocalDateTime fromDate, LocalDateTime toDate,
                                                       int page, int size) {
        TaskStatus taskStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                taskStatus = TaskStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Invalid task status: " + status);
            }
        }

        TaskPriority taskPriority = null;
        if (priority != null && !priority.isEmpty()) {
            try {
                taskPriority = TaskPriority.valueOf(priority);
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Invalid task priority: " + priority);
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasksPage = taskRepository.searchTasks(
                title, taskStatus, taskPriority, category, fromDate, toDate, pageable);

        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();

        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks retrieved successfully");
        response.setPath("/api/v1/tasks/search");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }
}
