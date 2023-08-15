package com.backend.taskmanager.controller;

import com.backend.taskmanager.jsonBody.SignInRequest;
import com.backend.taskmanager.jsonBody.SignUpRequest;
import com.backend.taskmanager.jwt.JwtCore;
import com.backend.taskmanager.model.User;
import com.backend.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private JwtCore jwtCore;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    @Autowired
    private void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @Autowired
    private void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handle POST request for creating a new user account.
     *
     * @param request comprised of JSON body with "username" & "password"
     * `SignUpRequest` DTO class used to capture request body.
     *
     * @return ResponseEntity of type String which holds the status of account creation
     * returns "Success" as body with HTTP status OK on successful creation,
     * returns "This username is already used" with HTTP status BAD_REQUEST if entered username is already in use.
     *
     * @see com.backend.taskmanager.jsonBody.SignUpRequest
     */
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This username is already used");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    /**
     * Handle POST requests on "/sign-in" endpoint for user signin process.
     *
     * @param request a `SignInRequest` object that holds the login credentials (username, password) of the user
     *
     * @return ResponseEntity with JWT token as body and HTTP status OK in case of successful sign-in,
     * returns "Incorrect credentials" with HTTP status UNAUTHORIZED, if entered credentials are wrong.
     *
     * @see com.backend.taskmanager.jsonBody.SignInRequest
     */
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        } catch (BadCredentialsException bce) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtCore.generateToken(auth);
        return ResponseEntity.ok().body(jwt);
    }
}
