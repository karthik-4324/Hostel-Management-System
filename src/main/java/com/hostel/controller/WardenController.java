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
@RequestMapping("/api/warden")
@CrossOrigin(origins = "*")
public class WardenController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getWardenDashboard(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
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

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            if (!"WARDEN".equals(userRole) && !"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> result = userService.getStudentsWithPagination(page, limit, search);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch students: " + e.getMessage()));
        }
    }
}