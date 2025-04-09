package com.project.task_management_app.payload.Response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private int totalWorkspaces;
    private int totalTasks;
    private TaskStats taskStats;
    private List<WorkspaceMembershipStatus> workspaceStats;
    private LoginStreakStats loginStreakStats;
    private List<Achievement> achievements;
}
