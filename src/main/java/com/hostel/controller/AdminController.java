package com.hostel.controller;

import com.hostel.model.User;
import com.hostel.service.UserService;
import com.hostel.service.DashboardService;
import com.hostel.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReportService reportService;

    private boolean isAdmin() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return false;
        }
        return "ADMIN".equals(attrs.getRequest().getAttribute("userRole"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard(HttpServletRequest request) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            String userId = (String) request.getAttribute("userId");
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

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
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

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@Valid @RequestBody User student) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            student.setRole("STUDENT");
            User savedStudent = userService.saveUser(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student created successfully");
            response.put("data", Map.of("student", savedStudent));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to create student: " + e.getMessage()));
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @Valid @RequestBody User studentDetails) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            User updatedStudent = userService.updateUser(id, studentDetails);

            if (updatedStudent == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Student not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student updated successfully");
            response.put("data", Map.of("student", updatedStudent));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update student: " + e.getMessage()));
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            userService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to delete student: " + e.getMessage()));
        }
    }

    @PostMapping("/assign-room")
    public ResponseEntity<?> assignRoom(@RequestBody Map<String, Object> assignmentData) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            String studentId = (String) assignmentData.get("studentId");
            String roomId = (String) assignmentData.get("roomId");
            Integer bedNumber = (Integer) assignmentData.get("bedNumber");

            boolean success = userService.assignStudentToRoom(studentId, roomId, bedNumber);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Room assigned successfully");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Failed to assign room"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to assign room: " + e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> generateReport(
            @RequestParam String type,
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            if (!isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            Map<String, Object> report = reportService.generateReport(type, format, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", report);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to generate report: " + e.getMessage()));
        }
    }
}