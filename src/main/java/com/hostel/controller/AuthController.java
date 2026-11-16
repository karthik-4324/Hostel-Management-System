package com.hostel.controller;

import com.hostel.model.User;
import com.hostel.security.JwtUtil;
import com.hostel.service.UserService;
import com.hostel.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email already exists"));
            }
            user.setRole("STUDENT");
            User savedUser = userService.saveUser(user);

            String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("token", token);
            response.put("user", savedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            Optional<User> userOptional = userService.getUserByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid email or password"));
            }

            User user = userOptional.get();

            if (!user.isActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Account is deactivated"));
            }

            // Validate password
            if (!userService.validatePassword(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid email or password"));
            }

            // Update last login
            userService.updateLastLogin(user.getId());

            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.requestReset(body.get("email"));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                    .error("Forgot password failed: {}", e.getMessage());
        }
        // Same answer whether or not the email exists
        return ResponseEntity.ok(Map.of("success", true,
                "message", "If an account exists for that email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetPassword(body.get("token"), body.get("password"));
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Password reset successfully. You can sign in now."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to reset password"));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "Please sign in again"));
            }
            passwordResetService.changePassword(userId, body.get("currentPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to change password"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            Optional<User> userOptional = userService.getUserById(userId);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "User not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userOptional.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody User userDetails) {
        try {
            String userId = (String) request.getAttribute("userId");
            User updatedUser = userService.updateUser(userId, userDetails);

            if (updatedUser == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "User not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update profile: " + e.getMessage()));
        }
    }
}