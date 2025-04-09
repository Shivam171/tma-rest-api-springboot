package com.project.task_management_app.services;

import com.project.task_management_app.enums.AssignmentStatus;
import com.project.task_management_app.enums.TaskStatus;
import com.project.task_management_app.enums.WorkspaceType;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.TaskAssignment;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import com.project.task_management_app.payload.Response.Dashboard.*;
import com.project.task_management_app.repositories.TaskAssignmentRepository;
import com.project.task_management_app.repositories.TaskRepository;
import com.project.task_management_app.repositories.UserRepository;
import com.project.task_management_app.repositories.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public DashboardResponse getUserDashboardData(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        int totalWorkspaces = user.getOwnedWorkspaces().size() + user.getMemberWorkspaces().size();
        int totalTasks = user.getCreatedTasks().size() + user.getAssignedTasks().size();

        TaskStats taskStats = getTaskStats(user);
        List<WorkspaceMembershipStatus> workspaceStats = getWorkspaceStats(user);
        LoginStreakStats loginStreakStats = calculateLoginStreak(user);
        List<Achievement> achievements = getAchievements(user);

        return new DashboardResponse(
                totalWorkspaces,
                totalTasks,
                taskStats,
                workspaceStats,
                loginStreakStats,
                achievements
        );
    }

    private TaskStats getTaskStats(User user) {
        // Calculate task stats (TODO, IN_PROGRESS, etc.) for the user
        int todoTasks = 0;
        int inProgressTasks = 0;
        int completedTasks = 0;
        int overdueTasks = 0;
        int upcomingTasks = 0;

        // Iterate through tasks created by the user
        for (Task task : user.getCreatedTasks()) {
            if (task.getStatus() == TaskStatus.TODO) {
                todoTasks++;
            } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
            } else if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            } else if (task.getStatus() == TaskStatus.OVERDUE) {
                overdueTasks++;
            } else if (task.getStatus() == TaskStatus.UPCOMING) {
                upcomingTasks++;
            }
        }

        // Iterate through tasks assigned to the user
        for (Task task : user.getAssignedTasks()) {
            if (task.getStatus() == TaskStatus.TODO) {
                todoTasks++;
            } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
            } else if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            } else if (task.getStatus() == TaskStatus.OVERDUE) {
                overdueTasks++;
            } else if (task.getStatus() == TaskStatus.UPCOMING) {
                upcomingTasks++;
            }
        }

        return new TaskStats(
                todoTasks + inProgressTasks + completedTasks + overdueTasks + upcomingTasks,
                todoTasks,
                inProgressTasks,
                completedTasks,
                overdueTasks,
                upcomingTasks
        );
    }

    private List<WorkspaceMembershipStatus> getWorkspaceStats(User user) {
        List<WorkspaceMembershipStatus> workspaceStats = new ArrayList<>();
        // Stats for workspaces owned by the user
        for (Workspace workspace : user.getOwnedWorkspaces()) {
            workspaceStats.add(new WorkspaceMembershipStatus(
                    workspace.getId(),
                    workspace.getName(),
                    workspace.getType() == WorkspaceType.PUBLIC,
                    AssignmentStatus.COMPLETED
            ));
        }

        // Stats for workspaces where the user is a member
        for (Workspace workspace : user.getMemberWorkspaces()) {
            AssignmentStatus overallStatus = calculateOverallAssignmentStatus(user, workspace);
            workspaceStats.add(new WorkspaceMembershipStatus(
                    workspace.getId(),
                    workspace.getName(),
                    workspace.getType() == WorkspaceType.PUBLIC,
                    overallStatus
            ));
        }
        return workspaceStats;
    }

    private AssignmentStatus calculateOverallAssignmentStatus(User user, Workspace workspace) {
        List<Task> workspaceTasks = taskRepository.findByWorkspace(workspace);
        boolean allCompleted = true;
        boolean anyInProgress = false;
        boolean anyPending = false;

        for (Task task : workspaceTasks) {
            TaskAssignment assignment = taskAssignmentRepository.findByTaskAndAssignee(task, user);
            if (assignment != null) {
                if (assignment.getStatus() != AssignmentStatus.COMPLETED) {
                    allCompleted = false;
                }
                if (assignment.getStatus() == AssignmentStatus.IN_PROGRESS) {
                    anyInProgress = true;
                }
                if (assignment.getStatus() == AssignmentStatus.PENDING) {
                    anyPending = true;
                }
            }
        }

        if (allCompleted) {
            return AssignmentStatus.COMPLETED;
        } else if (anyInProgress) {
            return AssignmentStatus.IN_PROGRESS;
        } else if (anyPending) {
            return AssignmentStatus.PENDING;
        } else {
            return AssignmentStatus.PENDING; // Default
        }
    }

    private LoginStreakStats calculateLoginStreak(User user) {
        if (user.getLoginHistory() == null || user.getLoginHistory().isEmpty()) {
            return new LoginStreakStats(0, 0, "Bronze", 1); // Default for no logins
        }

        List<LocalDateTime> loginHistory = user.getLoginHistory();
        loginHistory.sort(LocalDateTime::compareTo); // Ensure logins are in chronological order

        int currentStreak = 0;
        int longestStreak = 0;
        LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay(); // Today's date at 00:00
        LocalDateTime previousDate = null;

        for (LocalDateTime loginTime : loginHistory) {
            LocalDateTime loginDate = loginTime.toLocalDate().atStartOfDay();

            if (previousDate == null) {
                currentStreak = 1;
            } else if (loginDate.isEqual(previousDate.plusDays(1))) {
                currentStreak++;
            } else if (!loginDate.isEqual(previousDate)) {
                // Not consecutive, reset streak (but keep if it's the same day)
                currentStreak = 1;
            }

            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            previousDate = loginDate;
        }

        // Check if the last login was yesterday to continue the streak
        if (previousDate != null && currentDate.isEqual(previousDate.plusDays(1))) {
            currentStreak++;    // Continue the streak
        } else {
            currentStreak = 0; // Reset if not continuing from yesterday
        }
        if (previousDate != null && currentDate.isEqual(previousDate.plusDays(1))) {
            currentStreak++;
        }

        String nextBadge = "None";
        int daysToNextBadge = 0;

        // Example Badge Progression (adjust as desired)
        if (longestStreak < 7) {
            nextBadge = "Silver";
            daysToNextBadge = 7 - longestStreak;
        } else if (longestStreak < 14) {
            nextBadge = "Gold";
            daysToNextBadge = 14 - longestStreak;
        } else if (longestStreak < 21) {
            nextBadge = "Diamond";
            daysToNextBadge = 21 - longestStreak;
        } else {
            nextBadge = "Ruby"; // "Max Streak Reached"
        }

        return new LoginStreakStats(currentStreak, longestStreak, nextBadge, daysToNextBadge);
    }

    private List<Achievement> getAchievements(User user) {
        // Implement logic to determine which achievements are unlocked
        // based on user activity (e.g., tasks completed, login streak).
        // You'll need to define your achievement criteria.
        // For example:
        List<Achievement> achievements = new ArrayList<>();
        achievements.add(new Achievement("Bronze Starter", "Complete your first task", "/bronze.png", !user.getCreatedTasks().isEmpty()));
        achievements.add(new Achievement("Silver Streak", "Login for 7 consecutive days", "/silver.png", false)); // Placeholder
        achievements.add(new Achievement("Gold Taskmaster", "Complete 50 tasks", "/gold.png", user.getCreatedTasks().size() > 50));

        return achievements;
    }
}