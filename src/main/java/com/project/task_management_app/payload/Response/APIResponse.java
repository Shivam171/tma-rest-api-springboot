package com.project.task_management_app.payload.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {
    private T data;
    private String message;
    private boolean success;
    private int statusCode;
    private String method;
    private String path;
    private String timestamp;
}
