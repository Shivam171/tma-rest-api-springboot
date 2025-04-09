package com.project.task_management_app.payload.Request;

import com.project.task_management_app.enums.WorkspaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CreateWorkspaceRequest {
    @Pattern(
            regexp = "^[A-Za-z0-9-]+(?: [A-Za-z0-9-]+)*$",
            message = "Workspace name must be 3–50 characters, contain only letters, numbers, hyphens, single spaces, and no leading/trailing/multiple spaces"
    )
    @Size(min = 3, max = 50, message = "Workspace name must be between 3 and 50 characters")
    @NotBlank(message = "Workspace name is required")
    private String name;

    @NotNull(message = "Workspace description is required")
    private String description;

    private WorkspaceType type;
}
