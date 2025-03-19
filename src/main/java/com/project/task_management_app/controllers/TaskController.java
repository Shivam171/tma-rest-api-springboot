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
@RequestMapping("/api/v1/tasks")
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
            @RequestParam(defaultValue = "1") @Min(1)
            @Parameter(description = "Page number (1-based index)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of items per page") int size,

            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Field to sort by") String sortBy,

            @RequestParam(defaultValue = "desc")
            @Parameter(description = "Sort direction (asc/desc)") String direction) {

        APIResponse<List<TaskResponse>> response = taskService.getAllTasks(page, size, sortBy, direction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get task by ID
    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID", description = "Retrieve a task by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ResponseEntity<APIResponse<TaskResponse>> getTaskById(
            @PathVariable
            @Parameter(description = "UUID of the task") UUID taskId) {

        APIResponse<TaskResponse> response = taskService.getTaskById(taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Create a task
    @PostMapping
    @Operation(summary = "Create a new task", description = "Adds a new task to the system")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    public ResponseEntity<APIResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Task details") CreateTaskRequest request) {

        APIResponse<TaskResponse> response = taskService.createTask(userDetails, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update task
    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task", description = "Modify an existing task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ResponseEntity<APIResponse<TaskResponse>> updateTask(
            @PathVariable
            @Parameter(description = "UUID of the task to update") UUID taskId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated task details") UpdateTaskRequest request) {

        APIResponse<TaskResponse> response = taskService.updateTask(userDetails, taskId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Delete task
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", description = "Remove a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ResponseEntity<APIResponse<Void>> deleteTask(
            @PathVariable
            @Parameter(description = "UUID of the task to delete") UUID taskId) {

        APIResponse<Void> response = taskService.deleteTask(taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Search Tasks
    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Search tasks based on filters like title, status, priority, category, and date range with pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
    })
    public ResponseEntity<APIResponse<List<TaskResponse>>> searchTasks(
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
                title, status != null ? status.toString() : null,
                priority != null ? priority.toString() : null,
                category, fromDate, toDate, page, size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}