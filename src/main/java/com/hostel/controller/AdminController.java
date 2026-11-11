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

    @PostMapping("/students/{id}/vacate-room")
    public ResponseEntity<?> vacateRoom(@PathVariable String id) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            userService.vacateRoom(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room vacated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Failed to vacate room" : e.getMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", message));
        }
    }

    @GetMapping("/staff")
    public ResponseEntity<?> getStaff() {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            java.util.List<User> staff = new java.util.ArrayList<>();
            staff.addAll(userService.getUsersByRole("ADMIN"));
            staff.addAll(userService.getUsersByRole("WARDEN"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("staff", staff));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch staff"));
        }
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@Valid @RequestBody User staff) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            String role = staff.getRole() == null ? "" : staff.getRole().toUpperCase();
            if (!"WARDEN".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Role must be WARDEN or ADMIN"));
            }
            if (staff.getPassword() == null || staff.getPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password must be at least 8 characters"));
            }
            if (userService.existsByEmail(staff.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "An account with this email already exists"));
            }

            staff.setRole(role);
            staff.setRoomId(null);
            User saved = userService.saveUser(staff);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Staff account created successfully");
            response.put("data", Map.of("staff", saved));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Failed to create staff" : e.getMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", message));
        }
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable String id, HttpServletRequest request) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            String currentId = (String) request.getAttribute("userId");
            if (id.equals(currentId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "You cannot delete your own account"));
            }

            User target = userService.getUserById(id).orElse(null);
            if (target == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Staff account not found"));
            }
            if (!"WARDEN".equals(target.getRole()) && !"ADMIN".equals(target.getRole())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "This is not a staff account"));
            }
            if ("ADMIN".equals(target.getRole()) && userService.countUsersByRole("ADMIN") <= 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "You cannot delete the last admin"));
            }

            userService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Staff account removed");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Failed to remove staff" : e.getMessage();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", message));
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