package com.hostel.controller;

import com.hostel.model.User;
import com.hostel.service.UserService;
import com.hostel.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("")
    public ResponseEntity<?> getAllStudents(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can view all students
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
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

    @PostMapping("")
    public ResponseEntity<?> createStudent(@Valid @RequestBody User student, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can create students
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @Valid @RequestBody User studentDetails, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can update students
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin can delete students
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
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

    @PostMapping("/{id}/assign-room")
    public ResponseEntity<?> assignRoom(@PathVariable String id, @RequestBody Map<String, Object> assignmentData, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can assign rooms
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            String roomId = (String) assignmentData.get("roomId");
            Integer bedNumber = (Integer) assignmentData.get("bedNumber");
            
            boolean success = userService.assignStudentToRoom(id, roomId, bedNumber);
            
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
}