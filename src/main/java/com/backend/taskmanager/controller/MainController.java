package com.backend.taskmanager.controller;

import com.backend.taskmanager.jsonBody.CreateTaskRequest;
import com.backend.taskmanager.model.Task;
import com.backend.taskmanager.model.User;
import com.backend.taskmanager.repository.TaskRepository;
import com.backend.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    /**
     * Handle the GET requests to the "/user" endpoint to retrieve the user's information.
     *
     * @param principal the authenticated user who made the request.
     * @return the name of the authenticated user. Returns null if the user is unauthenticated.
     */
    @GetMapping("/")
    public ResponseEntity<?> userInfo(Principal principal) {
        if (principal == null) {
            return null;
        }
        return ResponseEntity.ok().body(principal.getName());

    }

    /**
     * Handle the GET requests on "/tasks" endpoint.
     *
     * @param principal the authenticated user who made the request.
     * @param from      the start index to retrieve tasks for the user (1-indexed, inclusive).Optional; defaults to 1
     *                  if not specified.
     * @param to        the end index to retrieve tasks for the user (1-indexed, inclusive). Optional; defaults to 10
     *                  if not specified.
     * @return a ResponseEntity containing a list of Tasks between the from and to indices (inclusive) belonging
     * to the authenticated user, and the HTTP status code. If any error condition happened(e.g., negative or zero
     * value at `from` or `to`, `from` value greater than `to`), returns a ResponseEntity with only the HTTP
     * status code (BAD_REQUEST).
     * @see org.springframework.security.core.Authentication#getPrincipal()
     * @see org.springframework.http.ResponseEntity
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(Principal principal,
                                      @RequestParam(required = false, defaultValue = "1") Integer from,
                                      @RequestParam(required = false, defaultValue = "10") Integer to) {
        User user = getUserByPrincipal(principal);

        if (from <= 0 || to <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        int offset = from - 1;

        int limit = to - from + 1;

        if (limit <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Task> tasks = taskRepository.findTasksByUserIdWithLimitAndOffset(user.getId(), limit, offset);

        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Handle POST requests on the "/new-task" endpoint to create a new task for the authenticated user.
     *
     * @param request   the request payload containing the properties for the new task. The details of the task are
     *                  taken from the CreateTaskRequest object which includes -
     *                  title: the title of the task,
     *                  deadline: the deadline for the task (in LocalDateTime format),
     *                  context: the context or description of the task.
     * @param principal the authenticated user who made the request.
     * @return a ResponseEntity with a string message indicating successful task creation and HTTP status OK,
     * or an appropriate error message and HTTP status in case of errors.
     * @see com.backend.taskmanager.jsonBody.CreateTaskRequest
     */
    @PostMapping("/new-task")
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, Principal principal) {
        User user = getUserByPrincipal(principal);

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setUser(user);
        task.setContext(request.getContext());
        task.setDeadline(request.getDeadline());

        taskRepository.save(task);
        return ResponseEntity.ok(String.format("Task %s was created", request.getTitle()));
    }
    /**
     * <p>Handle PATCH requests on "/edit-task/{id}" endpoint to partially update a Task.</p>
     *
     * <p>@param id the identifier of the Task to update.</p>
     *
     * <p>@param updates a Map where the keys are the names of the Task fields to update,
     * and the values are the new values for those fields.</p>
     *
     * <p>Task fields details:</p>
     * <ul>
     * <li>title: The title of the task,</li>
     * <li>isCompleted: Boolean indicates whether the task is completed,</li>
     * <li>isExpired: Boolean indicates whether the task is expired,</li>
     * <li>deadline: The deadline of the task (DateTime format),</li>
     * <li>context: The context or additional details of the task.</li>
     * </ul>
     *
     * <p>@return a ResponseEntity with the updated Task and HTTP status OK in case of successful update,
     * or a ResponseEntity with HTTP status NOT_FOUND if no Task with the given id exists.</p>
     *
     * @see org.springframework.web.bind.annotation.PatchMapping
     * @see org.springframework.http.ResponseEntity
     */
    @PatchMapping("/edit-task/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                                 @RequestBody Map<String, Object> updates) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Task task = optionalTask.get();

        if(updates.containsKey("title")){
            task.setTitle((String) updates.get("title"));
        }

        if(updates.containsKey("isCompleted")){
            task.setCompleted((Boolean.parseBoolean((String) updates.get("isCompleted"))));
        }

        if(updates.containsKey("isExpired")){
            task.setExpired((Boolean.parseBoolean((String) updates.get("isExpired"))));
        }

        if(updates.containsKey("deadline")){
            task.setDeadline(LocalDateTime.parse(updates.get("deadline").toString()));
        }

        if(updates.containsKey("context")){
            task.setContext((String) updates.get("context"));
        }

        taskRepository.save(task);

        return ResponseEntity.ok(task);
    }

    /**
     * Handle DELETE requests on "delete-task/{id}" endpoint to delete a Task.
     *
     * @param id the identifier of the Task to be deleted.
     *
     * @return a ResponseEntity with a confirmation message and HTTP status OK once the Task is successfully deleted.
     *
     * @see org.springframework.web.bind.annotation.DeleteMapping
     * @see org.springframework.http.ResponseEntity
     */
    @DeleteMapping("delete-task/{id}")
    public ResponseEntity<String > deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.ok("Successfully deleted");
    }


    /**
     * Helper method to get the User object from their authentication information.
     *
     * @param principal the authenticated user information
     * @return a User object representing the authenticated user
     * @throws UsernameNotFoundException if no user is found with the given username
     * @see Principal
     * @see User
     */
    private User getUserByPrincipal(Principal principal) {
        Optional<User> optionalUser = userRepository.findUserByUsername(principal.getName());

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

}
