package com.project.task_management_app.payload.Response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginStreakStats {
    private int currentStreak;
    private int longestStreak;
    private String nextBadge; // e.g., "Gold"
    private int daysToNextBadge;
}