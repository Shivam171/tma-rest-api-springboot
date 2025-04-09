package com.project.task_management_app.payload.Response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStats {
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int overdueTasks;
    private int upcomingTasks;
}
