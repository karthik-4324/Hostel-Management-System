package com.hostel.controller;

import com.hostel.service.DashboardService;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminDashboard(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> dashboardData = dashboardService.getAdminDashboardStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dashboardData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch admin dashboard: " + e.getMessage()));
        }
    }

    @GetMapping("/warden")
    public ResponseEntity<?> getWardenDashboard(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            if (!"WARDEN".equals(userRole) && !"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> dashboardData = dashboardService.getWardenDashboardStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dashboardData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch warden dashboard: " + e.getMessage()));
        }
    }

    @PostMapping("/student/leave-room")
    public ResponseEntity<?> leaveRoom(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            if (!"STUDENT".equals(userRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "Only students can use this"));
            }

            userService.vacateRoom(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "You have left your room");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Failed to leave room" : e.getMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", message));
        }
    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudentDashboard(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            if (!"STUDENT".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> dashboardData = dashboardService.getStudentDashboardStats(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dashboardData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch student dashboard: " + e.getMessage()));
        }
    }
}