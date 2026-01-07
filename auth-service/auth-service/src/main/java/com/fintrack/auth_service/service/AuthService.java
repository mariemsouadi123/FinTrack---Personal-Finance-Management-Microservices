package com.fintrack.auth_service.service;

import com.fintrack.auth_service.config.JwtUtil;
import com.fintrack.auth_service.entities.User;
import com.fintrack.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public Map<String, Object> registerUser(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);

        // Generate REAL JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getUserId());

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", Map.of(
                "id", savedUser.getUserId(),
                "name", savedUser.getName(),
                "email", savedUser.getEmail()
        ));
        response.put("token", token);

        return response;
    }

    public Map<String, Object> loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Verify password with encryption
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", userInfo);
        response.put("token", token);

        return response;
    }

    public Map<String, Object> validateToken(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);

                response.put("valid", true);
                response.put("userId", userId);
                response.put("username", username);
            } else {
                response.put("valid", false);
                response.put("error", "Invalid or expired token");
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
}