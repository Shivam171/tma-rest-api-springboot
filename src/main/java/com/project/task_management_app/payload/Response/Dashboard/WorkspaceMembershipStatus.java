package com.project.task_management_app.payload.Response.Dashboard;

import com.project.task_management_app.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMembershipStatus {
    private UUID workspaceId;
    private String workspaceName;
    private boolean isPublic;
    private AssignmentStatus overallAssignmentStatus;
}
