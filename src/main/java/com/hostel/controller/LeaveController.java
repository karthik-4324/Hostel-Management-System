package com.hostel.controller;

import com.hostel.model.Leave;
import com.hostel.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("")
    public ResponseEntity<?> getAllLeaves(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            Map<String, Object> result = leaveService.getAllLeaves(userId, userRole, page, limit, search, status, leaveType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch leaves: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeave(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            Leave leave = leaveService.getLeaveById(id);
            
            if (leave == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Leave not found"));
            }
            
            // Check permissions for students
            if ("STUDENT".equals(userRole) && !leave.getStudentId().equals(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("leave", leave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch leave: " + e.getMessage()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createLeave(@Valid @RequestBody Leave leave, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            // Only students can create leave applications
            if (!"STUDENT".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Only students can apply for leave"));
            }
            
            leave.setStudentId(userId);
            Leave savedLeave = leaveService.createLeave(leave);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave application submitted successfully");
            response.put("data", Map.of("leave", savedLeave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to create leave: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeave(@PathVariable String id, @Valid @RequestBody Leave leaveDetails, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Leave not found"));
            }
            
            // Students can only update their own pending leaves
            if ("STUDENT".equals(userRole)) {
                if (!existingLeave.getStudentId().equals(userId) || !"PENDING".equals(existingLeave.getStatus())) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
                }
            }
            
            Leave updatedLeave = leaveService.updateLeave(id, leaveDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave updated successfully");
            response.put("data", Map.of("leave", updatedLeave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update leave: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLeaveStatus(
            @PathVariable String id, 
            @RequestBody Map<String, String> statusUpdate,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can update status
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            String status = statusUpdate.get("status");
            String comment = statusUpdate.get("comment");
            
            Leave updatedLeave = leaveService.updateLeaveStatus(id, status, userId, comment);
            
            if (updatedLeave == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Leave not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave status updated successfully");
            response.put("data", Map.of("leave", updatedLeave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update leave status: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(@PathVariable String id, @RequestBody Map<String, String> cancelData, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            String reason = cancelData.get("reason");
            
            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Leave not found"));
            }
            
            // Students can only cancel their own leaves
            if ("STUDENT".equals(userRole) && !existingLeave.getStudentId().equals(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            Leave cancelledLeave = leaveService.updateLeaveStatus(id, "CANCELLED", userId, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave cancelled successfully");
            response.put("data", Map.of("leave", cancelledLeave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to cancel leave: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> markReturn(@PathVariable String id, @RequestBody Map<String, String> returnData, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can mark return
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            String returnDateStr = returnData.get("returnDate");
            LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : LocalDate.now();
            
            Leave updatedLeave = leaveService.markReturn(id, returnDate);
            
            if (updatedLeave == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Leave not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Return marked successfully");
            response.put("data", Map.of("leave", updatedLeave));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to mark return: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getLeaveStats(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can view stats
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            Map<String, Object> stats = leaveService.getLeaveStats(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch leave stats: " + e.getMessage()));
        }
    }
}