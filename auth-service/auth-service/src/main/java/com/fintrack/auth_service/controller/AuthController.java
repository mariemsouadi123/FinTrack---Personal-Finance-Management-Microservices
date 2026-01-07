package com.fintrack.auth_service.controller;

import com.fintrack.auth_service.entities.User;
import com.fintrack.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fintrack.auth_service.entities.ValidationRequest; // Add this


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Health check (public)
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service",
                "security", "JWT-enabled"
        ));
    }

    // Register new user (public)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Map<String, Object> response = authService.registerUser(user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Login user (public)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email and password are required"));
            }

            Map<String, Object> response = authService.loginUser(email, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Validate token (public) - FIX THIS METHOD
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody ValidationRequest request) {
        try {
            String token = request.getToken();

            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token is required"));
            }

            Map<String, Object> response = authService.validateToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add this new endpoint for user validation
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody ValidationRequest request) {
        try {
            String token = request.getToken();
            Long userId = request.getUserId();

            if (token == null || userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token and userId are required"));
            }

            Map<String, Object> validationResult = authService.validateToken(token);

            if (!(Boolean) validationResult.get("valid")) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "error", validationResult.get("error")
                ));
            }

            // Check if token's userId matches requested userId
            Long tokenUserId = ((Number) validationResult.get("userId")).longValue();
            boolean isValid = tokenUserId.equals(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("userId", tokenUserId);
            response.put("username", validationResult.get("username"));

            if (!isValid) {
                response.put("error", "User ID mismatch");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get current user profile (requires authentication)
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Map<String, Object> validationResult = authService.validateToken(token);

            // Check if token is valid
            if (!(Boolean) validationResult.get("valid")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }

            // Extract user ID correctly
            Long userId = ((Number) validationResult.get("userId")).longValue();

            User user = authService.getUserById(userId);

            return ResponseEntity.ok(Map.of(
                    "id", user.getUserId(),
                    "name", user.getName(),
                    "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token or user not found: " + e.getMessage()));
        }
    }

    // Get all users (requires authentication - admin only in future)
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(authService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}