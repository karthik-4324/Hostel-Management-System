package com.hostel.controller;

import com.hostel.model.Leave;
import com.hostel.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    private ResponseEntity<?> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", message));
    }

    private ResponseEntity<?> invalid(String message) {
        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", message));
    }

    private boolean isStaff(String role) {
        return "ADMIN".equals(role) || "WARDEN".equals(role);
    }

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

            Map<String, Object> result = leaveService.getAllLeaves(userId, userRole, page, limit, search, status,
                    leaveType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to fetch leaves: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeave(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Leave leave = leaveService.getLeaveById(id);
            if (leave == null) {
                return invalid("Leave not found");
            }

            if ("STUDENT".equals(userRole) && !userId.equals(leave.getStudentId())) {
                return forbidden("Access denied");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("leave", leave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to fetch leave: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createLeave(@RequestBody Leave leave, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            if (!"STUDENT".equals(userRole)) {
                return forbidden("Only students can apply for leave");
            }
            if (leave.getStartDate() == null || leave.getEndDate() == null) {
                return invalid("Start date and end date are required");
            }
            if (leave.getEndDate().isBefore(leave.getStartDate())) {
                return invalid("End date cannot be before the start date");
            }
            if (leave.getStartDate().isBefore(LocalDate.now())) {
                return invalid("Start date cannot be in the past");
            }
            if (leave.getReason() == null || leave.getReason().trim().isEmpty()) {
                return invalid("Reason is required");
            }
            if (leave.getReason().length() > 500) {
                return invalid("Reason cannot exceed 500 characters");
            }

            leave.setStudentId(userId);
            leave.setStatus("PENDING");
            leave.setApprovedBy(null);
            leave.setApprovedDate(null);
            leave.setRejectionReason(null);

            Leave savedLeave = leaveService.createLeave(leave);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave application submitted successfully");
            response.put("data", Map.of("leave", savedLeave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to create leave: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeave(@PathVariable String id, @RequestBody Leave leaveDetails,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return invalid("Leave not found");
            }
            if (!"STUDENT".equals(userRole) || !userId.equals(existingLeave.getStudentId())) {
                return forbidden("You can only edit your own leave applications");
            }
            if (!"PENDING".equals(existingLeave.getStatus())) {
                return forbidden("Only pending leave applications can be edited");
            }
            if (leaveDetails.getReason() != null && leaveDetails.getReason().length() > 500) {
                return invalid("Reason cannot exceed 500 characters");
            }

            Leave updatedLeave = leaveService.updateLeave(id, leaveDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave updated successfully");
            response.put("data", Map.of("leave", updatedLeave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to update leave: " + e.getMessage());
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

            if (!isStaff(userRole)) {
                return forbidden("Only the admin or warden can approve or reject leaves");
            }

            String status = statusUpdate.get("status") == null ? "" : statusUpdate.get("status").toUpperCase();
            String comment = statusUpdate.get("comment");

            if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
                return invalid("Status must be APPROVED or REJECTED");
            }
            if ("REJECTED".equals(status) && (comment == null || comment.trim().isEmpty())) {
                return invalid("A reason is required to reject a leave");
            }

            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return invalid("Leave not found");
            }
            if (!"PENDING".equals(existingLeave.getStatus())) {
                return invalid("Only pending leaves can be approved or rejected");
            }

            Leave updatedLeave = leaveService.updateLeaveStatus(id, status, userId, comment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave " + status.toLowerCase() + " successfully");
            response.put("data", Map.of("leave", updatedLeave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to update leave status: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(@PathVariable String id, @RequestBody Map<String, String> cancelData,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            String reason = cancelData.get("reason");

            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return invalid("Leave not found");
            }
            if (!"STUDENT".equals(userRole) || !userId.equals(existingLeave.getStudentId())) {
                return forbidden("You can only cancel your own leave applications");
            }
            if (!"PENDING".equals(existingLeave.getStatus())) {
                return forbidden("Only pending leave applications can be cancelled");
            }

            Leave cancelledLeave = leaveService.updateLeaveStatus(id, "CANCELLED", userId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave cancelled successfully");
            response.put("data", Map.of("leave", cancelledLeave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to cancel leave: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> markReturn(@PathVariable String id, @RequestBody Map<String, String> returnData,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            if (!isStaff(userRole)) {
                return forbidden("Access denied");
            }

            Leave existingLeave = leaveService.getLeaveById(id);
            if (existingLeave == null) {
                return invalid("Leave not found");
            }
            if (!"APPROVED".equals(existingLeave.getStatus())) {
                return invalid("Only approved leaves can be marked as returned");
            }

            String returnDateStr = returnData.get("returnDate");
            LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : LocalDate.now();

            Leave updatedLeave = leaveService.markReturn(id, returnDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Return marked successfully");
            response.put("data", Map.of("leave", updatedLeave));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to mark return: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeave(@PathVariable String id, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            if (!"ADMIN".equals(userRole)) {
                return forbidden("Only the admin can delete leave records");
            }

            leaveService.deleteLeave(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to delete leave: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getLeaveStats(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            if (!isStaff(userRole)) {
                return forbidden("Access denied");
            }

            Map<String, Object> stats = leaveService.getLeaveStats(year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return invalid("Failed to fetch leave stats: " + e.getMessage());
        }
    }
}