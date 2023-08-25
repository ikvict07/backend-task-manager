package com.backend.taskmanager.jsonBody;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String password;
}
