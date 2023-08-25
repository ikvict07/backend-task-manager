package com.backend.taskmanager.jsonBody;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {
    private String title;
    private LocalDateTime deadline;
    private String context;
}
