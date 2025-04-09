package com.project.task_management_app.controllers;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import com.project.task_management_app.payload.Request.CreateTaskRequest;
import com.project.task_management_app.payload.Request.UpdateTaskRequest;
import com.project.task_management_app.payload.Response.APIResponse;
import com.project.task_management_app.payload.Response.TaskResponse;
import com.project.task_management_app.services.TaskService;
import com.project.task_management_app.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {
    private final TaskService taskService;

    // Get all tasks with pagination and sorting
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieve a paginated list of tasks with sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
    public ResponseEntity<APIResponse<List<TaskResponse>>> getAllTasks(
            @PathVariable UUID workspaceId,

            @RequestParam(defaultValue = "1") @Min(1)
            @Parameter(description = "Page number (1-based index)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of items per page") int size,

            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Field to sort by") String sortBy,

            @RequestParam(defaultValue = "desc")
            @Parameter(description = "Sort direction (asc/desc)") String direction) {

        APIResponse<List<TaskResponse>> response = taskService.getAllTasks(workspaceId, page, size, sortBy, direction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get task by ID
    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID in a workspace", description = "Retrieve a task by its unique identifier within a specific workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ResponseEntity<APIResponse<TaskResponse>> getTaskById(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "UUID of the task") UUID taskId) {

        APIResponse<TaskResponse> response = taskService.getTaskById(workspaceId,taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Create a task
    @PostMapping
    @Operation(summary = "Create a new task in a workspace", description = "Adds a new task to the specified workspace")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    public ResponseEntity<APIResponse<TaskResponse>> createTask(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Task details") CreateTaskRequest request) {

        APIResponse<TaskResponse> response = taskService.createTask(workspaceId, userDetails, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update task
    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task in a workspace", description = "Modify an existing task by its ID within a specific workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - not authorized to update this task", content = @Content)
    })
    public ResponseEntity<APIResponse<TaskResponse>> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "UUID of the task to update") UUID taskId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated task details") UpdateTaskRequest request) {

        APIResponse<TaskResponse> response = taskService.updateTask(workspaceId, userDetails, taskId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Delete task
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task from a workspace", description = "Remove a task by its ID from a specific workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ResponseEntity<APIResponse<Void>> deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "UUID of the task to delete") UUID taskId) {

        APIResponse<Void> response = taskService.deleteTask(workspaceId, taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get tasks by status
    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status in a workspace", description = "Retrieve all tasks with a specific status within a workspace")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
    public ResponseEntity<APIResponse<List<TaskResponse>>> getTasksByStatus(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "Status of the tasks to retrieve", example = "TODO") TaskStatus status) {

        APIResponse<List<TaskResponse>> response = taskService.getTasksByStatus(workspaceId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get tasks by priority
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority in a workspace", description = "Retrieve all tasks with a specific priority within a workspace")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
    public ResponseEntity<APIResponse<List<TaskResponse>>> getTasksByPriority(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "Priority of the tasks to retrieve", example = "HIGH") TaskPriority priority) {

        APIResponse<List<TaskResponse>> response = taskService.getTasksByPriority(workspaceId, priority);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get tasks by category
    @GetMapping("/category/{category}")
    @Operation(summary = "Get tasks by category in a workspace", description = "Retrieve all tasks in a specific category within a workspace")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
    public ResponseEntity<APIResponse<List<TaskResponse>>> getTasksByCategory(
            @PathVariable UUID workspaceId,
            @PathVariable @Parameter(description = "Category of the tasks to retrieve", example = "Work") String category) {

        APIResponse<List<TaskResponse>> response = taskService.getTasksByCategory(workspaceId, category);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get recent tasks
    @GetMapping("/recent")
    @Operation(summary = "Get recent tasks from a workspace", description = "Retrieve 5 most recently created tasks within a workspace")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved recent tasks")
    public ResponseEntity<APIResponse<List<TaskResponse>>> getRecentTasks(@PathVariable UUID workspaceId) {
        APIResponse<List<TaskResponse>> response = taskService.getRecentTasks(workspaceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Search Tasks
    @GetMapping("/search")
    @Operation(summary = "Search tasks in a workspace", description = "Search tasks based on filters like title, status, priority, category, and date range with pagination, within a specific workspace.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
    })
    public ResponseEntity<APIResponse<List<TaskResponse>>> searchTasks(
            @PathVariable UUID workspaceId,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks by title") String title,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks by status (e.g., TODO, IN_PROGRESS, COMPLETED)", example = "TODO") TaskStatus status,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks by priority (e.g., HIGH, MEDIUM, LOW)", example = "HIGH") TaskPriority priority,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks by category (e.g., Work, Personal)") String category,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks created after this date (format: YYYY-MM-DDTHH:mm:ss)", example = "2025-03-01T00:00:00") LocalDateTime fromDate,

            @RequestParam(required = false)
            @Parameter(description = "Filter tasks created before this date (format: YYYY-MM-DDTHH:mm:ss)", example = "2025-03-18T23:59:59") LocalDateTime toDate,

            @RequestParam(defaultValue = "1")
            @Parameter(description = "Page number (1-based index)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of tasks per page") int size) {

        APIResponse<List<TaskResponse>> response = taskService.searchTasks(
                workspaceId, title, status != null ? status.toString() : null,
                priority != null ? priority.toString() : null,
                category, fromDate, toDate, page, size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}