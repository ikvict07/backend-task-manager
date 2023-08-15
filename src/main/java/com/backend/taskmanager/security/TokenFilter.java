package com.backend.taskmanager.security;

import com.backend.taskmanager.jwt.JwtCore;
import com.backend.taskmanager.service.UserServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private JwtCore jwtCore;
    private UserServiceImpl userService;

    @Autowired
    private void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @Autowired
    private void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwt(request);
            if (jwt != null) {
                String username = getUsername(jwtCore, jwt);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    authenticate(userService, username);
                }
            }
        } catch (Exception ignored) {}
        filterChain.doFilter(request, response);
    }

    private static String getJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private static String getUsername(JwtCore jwtCore, String jwt) {
        String username = null;
        try {
             username = jwtCore.getNameFromJwt(jwt);
        } catch (ExpiredJwtException ignored) {}
        return username;
    }

    private static void authenticate(UserServiceImpl userService, String username) {
        UserDetails userDetails = userService.loadUserByUsername(username);
        SecurityContextHolder
                .getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null));
    }
}

