package com.project.task_management_app.payload.Response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {
    private String badgeName;
    private String description;
    private String imageUrl;
    private boolean isUnlocked;
}
