package com.backend.taskmanager.controller;

import com.backend.taskmanager.model.Task;
import com.backend.taskmanager.model.User;
import com.backend.taskmanager.repository.TaskRepository;
import com.backend.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class MainController {
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    @Autowired
    private void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Autowired
    private void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public ResponseEntity<?> userInfo(Principal principal) {
        if (principal == null) {
            return null;
        }
        return ResponseEntity.ok().body(principal.getName());

    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(Principal principal) {
        Optional<User> optionalUser = userRepository.findUserByUsername(principal.getName());

        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = optionalUser.get();

        List<Task> taskList = taskRepository.findTasksByUser(user);

        if (taskList.isEmpty()) {
            return new ResponseEntity<>("No tasks were found", HttpStatus.OK);
        }

        return new ResponseEntity<>(taskList, HttpStatus.OK);
    }

}
