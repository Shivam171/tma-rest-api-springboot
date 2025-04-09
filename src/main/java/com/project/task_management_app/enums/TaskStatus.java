package com.project.task_management_app.enums;

public enum TaskStatus {
    UPCOMING,     // Not active yet, future start date maybe
    TODO,         // Ready to be picked up
    IN_PROGRESS,  // Work has started
    COMPLETED,    // Everyone has finished
    OVERDUE       // Due date passed, not finished
}