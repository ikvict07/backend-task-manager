package com.backend.taskmanager.controller;

import com.backend.taskmanager.jsonBody.CreateTaskRequest;
import com.backend.taskmanager.model.Task;
import com.backend.taskmanager.model.User;
import com.backend.taskmanager.repository.TaskRepository;
import com.backend.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MainControllerTest {

    @InjectMocks
    MainController mainController;
    @Mock
    Principal principal;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TaskRepository taskRepository;

    @BeforeEach
    public void setup() throws Exception {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
        }

    }

    @Test
    void userInfoTest() {
        String username = "test";
        when(principal.getName()).thenReturn(username);
        assertEquals(ResponseEntity.ok().body(principal.getName()), mainController.userInfo(principal));
    }

    @Test
    void getTasksTest() {
        String username = "test";
        when(principal.getName()).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(new User()));
        assertEquals(ResponseEntity.ok().body(List.of()), mainController.getTasks(principal, 1, 10));
        assertEquals(new ResponseEntity<>(HttpStatus.BAD_REQUEST), mainController.getTasks(principal, -1, 10));
        assertEquals(new ResponseEntity<>(HttpStatus.BAD_REQUEST), mainController.getTasks(principal, 5, 3));


        when(principal.getName()).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> mainController.getTasks(principal, 1, 10));
    }

    @Test
    void createTaskTest() {
        String username = "test";
        when(principal.getName()).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(new User()));
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        // Возможно, вам потребуется добавить значения в taskRequest - в зависимости от того, как они используются в вашем коде
        assertEquals(ResponseEntity.ok(String.format("Task %s was created", taskRequest.getTitle())), mainController.createTask(taskRequest, principal));
    }

    @Test
    void deleteTaskTest() {
        String username = "test";
        Long id = 1L;
        when(principal.getName()).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(new User()));

        assertEquals(ResponseEntity.ok("Successfully deleted"), mainController.deleteTask(id));
    }

    @Test
    void updateTaskTest() {
        Task oldTask = new Task();
        oldTask.setId(1L);
        oldTask.setTitle("Old Task");
        oldTask.setDeadline(LocalDateTime.parse("2023-12-05T01:16:30"));
        oldTask.setContext("old context");
        oldTask.setExpired(false);
        oldTask.setCompleted(false);


        Map<String, Object> updates = new HashMap<>();
        String newTitle = "New Title";
        String newContext = "New Context";
        String newIsExpired = "true";
        String newIsCompleted = "true";
        LocalDateTime newDeadline = LocalDateTime.parse("2023-12-05T01:16:30");
        updates.put("title", newTitle);
        updates.put("context", newContext);
        updates.put("deadline", newDeadline);
        updates.put("isExpired", newIsExpired);
        updates.put("isCompleted", newIsCompleted);

        when(taskRepository.findById(oldTask.getId())).thenReturn(Optional.of(oldTask));

        mainController.updateTask(oldTask.getId(), updates);

        assertEquals(newTitle, oldTask.getTitle());
        assertEquals(newContext, oldTask.getContext());
        assertEquals(newDeadline, oldTask.getDeadline());
        assertTrue(oldTask.isExpired());
        assertTrue(oldTask.isCompleted());

        verify(taskRepository, times(1)).save(any(Task.class));

        when(taskRepository.findById(2L)).thenReturn(Optional.empty());
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build(), mainController.updateTask(2L, updates));
    }
}