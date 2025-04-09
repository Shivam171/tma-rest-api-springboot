package com.project.task_management_app.services;

import com.project.task_management_app.enums.AssignmentStatus;
import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import com.project.task_management_app.exceptions.AccessDeniedException;
import com.project.task_management_app.exceptions.InvalidRequestException;
import com.project.task_management_app.exceptions.ResourceNotFoundException;
import com.project.task_management_app.exceptions.TaskAlreadyExistsException;
import com.project.task_management_app.mapper.TaskMapper;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.TaskAssignment;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Request.CreateTaskRequest;
import com.project.task_management_app.payload.Request.UpdateTaskRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.TaskResponse;
import com.project.task_management_app.repositories.TaskAssignmentRepository;
import com.project.task_management_app.repositories.TaskRepository;

import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.repositories.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.project.task_management_app.mapper.TaskMapper.mapToTaskResponse;

@Service
@RequiredArgsConstructor
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final WorkspaceRepository workspaceRepository;

    // Get all tasks
    public APIResponse<List<TaskResponse>> getAllTasks(
            UUID workspaceId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(sortDirection, sortBy));

        Page<Task> tasksPage = taskRepository.findByWorkspace(workspace, pageable);
        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();
        APIResponse<List<TaskResponse>> response = new APIResponse<>();

        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Get task by id
    public APIResponse<TaskResponse> getTaskById(UUID workspaceId, UUID taskId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        Task task = taskRepository.findByIdAndWorkspace(taskId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(task));
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Task retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Create task
    public APIResponse<TaskResponse> createTask(UUID workspaceId, UserDetailsImpl userDetails, CreateTaskRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        if (!workspace.getMembers().contains(user)) {
            throw new AccessDeniedException("You are not a member of this workspace");
        }

        // Check if task with same title already exists in the workspace
        Optional<Task> existingTask = taskRepository.findByTitle(request.getTitle());
        if (existingTask.isPresent()) {
            throw new TaskAlreadyExistsException("Task with title '" + request.getTitle() + "' already exists in this workspace");
        }

        Set<User> assignees = new HashSet<>();
        if (request.getAssigneeIds() != null) {
            assignees = request.getAssigneeIds().stream()
                    .map(id -> userRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id)))
                    .collect(Collectors.toSet());
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
        task.setWorkspace(workspace);
        task.setAssignees(assignees);

        Task savedTask = taskRepository.save(task);

        // Create TaskAssignment for each assignee
        for (User assignee : assignees) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTask(savedTask);
            assignment.setAssignee(assignee);
            assignment.setStatus(AssignmentStatus.PENDING);
            assignment.setAssignedAt(LocalDateTime.now());
            taskAssignmentRepository.save(assignment);
        }

        // Recalculate status after assignments
        updateGlobalStatus(savedTask);

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(savedTask));
        response.setSuccess(true);
        response.setStatusCode(201);
        response.setMethod("POST");
        response.setMessage("Task created successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Update task
    public APIResponse<TaskResponse> updateTask(UUID workspaceId, UserDetailsImpl userDetails, UUID taskId, UpdateTaskRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Ensure that the logged-in user is the task owner
        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this task");
        }

        // Check if title is being changed and if new title already exists
        if (request.getTitle() != null && !task.getTitle().equals(request.getTitle())) {
            Optional<Task> taskWithSameTitle = taskRepository.findByTitleAndWorkspace(request.getTitle(), workspace);
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


        if (request.getAssigneeIds() != null) {
            Set<User> newAssignees = request.getAssigneeIds().stream()
                    .map(id -> userRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id)))
                    .collect(Collectors.toSet());

            Set<User> oldAssignees = task.getAssignees();

            // Remove assignments for users no longer assigned
            for (User oldAssignee : oldAssignees) {
                if (!newAssignees.contains(oldAssignee)) {
                    taskAssignmentRepository.deleteByTaskAndAssignee(task, oldAssignee);
                }
            }

            // Add assignments for new users
            for (User newAssignee : newAssignees) {
                if (!oldAssignees.contains(newAssignee)) {
                    TaskAssignment newAssignment = new TaskAssignment();
                    newAssignment.setTask(task);
                    newAssignment.setAssignee(newAssignee);
                    newAssignment.setStatus(AssignmentStatus.PENDING);
                    newAssignment.setAssignedAt(LocalDateTime.now());
                    taskAssignmentRepository.save(newAssignment);
                }
            }

            task.setAssignees(newAssignees);
        }

        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        updateGlobalStatus(updatedTask);

        APIResponse<TaskResponse> response = new APIResponse<>();
        response.setData(mapToTaskResponse(updatedTask));
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("PUT");
        response.setMessage("Task updated successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Recalculate task status
    public TaskStatus recalculateTaskStatus(Task task, List<TaskAssignment> assignments) {
        boolean allCompleted = assignments.stream()
                .allMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED);

        boolean anyInProgress = assignments.stream()
                .anyMatch(a -> a.getStatus() == AssignmentStatus.IN_PROGRESS);

        boolean anyPending = assignments.stream()
                .anyMatch(a -> a.getStatus() == AssignmentStatus.PENDING);

        if (allCompleted) return TaskStatus.COMPLETED;
        if (task.getDueDate().isBefore(LocalDateTime.now())) return TaskStatus.OVERDUE;
        if (anyInProgress) return TaskStatus.IN_PROGRESS;
        if (anyPending) return TaskStatus.TODO;
        return TaskStatus.UPCOMING;
    }

    // Update global task status
    public void updateGlobalStatus(Task task) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTask(task);
        TaskStatus updated = recalculateTaskStatus(task, assignments);
        if (task.getStatus() != updated) {
            task.setStatus(updated);
            taskRepository.save(task);
        }
    }

    // Scheduled job to auto update statuses (optional)
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void refreshAllTaskStatuses() {
        int page = 0;
        int size = 100; // Process 100 tasks at a time
        Page<Task> taskPage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            taskPage = taskRepository.findAll(pageable);

            for (Task task : taskPage.getContent()) {
                updateGlobalStatus(task);
            }

            page++;
        } while (page < taskPage.getTotalPages());
    }

    // Delete task
    public APIResponse<Void> deleteTask(UUID workspaceId, UUID taskId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        Task task = taskRepository.findByIdAndWorkspace(taskId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);

        APIResponse<Void> response = new APIResponse<>();
        response.setData(null);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("DELETE");
        response.setMessage("Task deleted successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/" + taskId);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Get tasks by status
    public APIResponse<List<TaskResponse>> getTasksByStatus(UUID workspaceId, TaskStatus status) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        List<Task> tasks = taskRepository.findByWorkspaceAndStatus(workspace, status);

        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();

        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks with status " + status + " retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/status/" + status);
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Get tasks by priority
    public APIResponse<List<TaskResponse>> getTasksByPriority(UUID workspaceId, TaskPriority priority) {  // Added workspaceId
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        List<Task> tasks = taskRepository.findByWorkspaceAndPriority(workspace, priority);  // Updated to find by workspace and priority
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();
        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks with priority " + priority + " retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/priority/" + priority);  // Updated path
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Get tasks by category
    public APIResponse<List<TaskResponse>> getTasksByCategory(UUID workspaceId, String category) {  // Added workspaceId
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        List<Task> tasks = taskRepository.findByWorkspaceAndCategory(workspace, category);  // Updated to find by workspace and category
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();
        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks with category '" + category + "' retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/category/" + category);  // Updated path
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Get recent tasks
    public APIResponse<List<TaskResponse>> getRecentTasks(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        List<Task> tasks = taskRepository.findRecentTasksByWorkspace(workspace);
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();
        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Recent tasks retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/recent");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }

    // Search Tasks
    public APIResponse<List<TaskResponse>> searchTasks(UUID workspaceId, String title, String status, String priority,
                                                       String category, LocalDateTime fromDate, LocalDateTime toDate,
                                                       int page, int size) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

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
        Page<Task> tasksPage = taskRepository.searchTasksByWorkspace(
                workspace, title, taskStatus, taskPriority, category, fromDate, toDate, pageable);
        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(TaskMapper::mapToTaskResponse)
                .toList();
        APIResponse<List<TaskResponse>> response = new APIResponse<>();
        response.setData(taskResponses);
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setMethod("GET");
        response.setMessage("Tasks retrieved successfully");
        response.setPath("/api/v1/workspaces/" + workspaceId + "/tasks/search");
        response.setTimestamp(String.valueOf(LocalDateTime.now()));

        return response;
    }


}
