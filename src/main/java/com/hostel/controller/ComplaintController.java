package com.hostel.controller;

import com.hostel.model.Complaint;
import com.hostel.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("")
    public ResponseEntity<?> getAllComplaints(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Map<String, Object> result = complaintService.getAllComplaints(userId, userRole, page, limit, status,
                    category, priority);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch complaints: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaint(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Optional<Complaint> complaintOpt = complaintService.getComplaintById(id);
            if (complaintOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Complaint not found"));
            }

            Complaint complaint = complaintOpt.get();

            // Check permissions for students
            if ("STUDENT".equals(userRole) && !complaint.getReportedBy().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("complaint", complaint));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch complaint: " + e.getMessage()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createComplaint(@Valid @RequestBody Complaint complaint, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            if (!"STUDENT".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Only students can submit complaints"));
            }
            complaint.setReportedBy(userId);

            Complaint savedComplaint = complaintService.createComplaint(complaint);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint created successfully");
            response.put("data", Map.of("complaint", savedComplaint));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to create complaint: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(
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

            Complaint updatedComplaint = complaintService.updateComplaintStatus(id, status, userId, comment);

            if (updatedComplaint == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Complaint not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint status updated successfully");
            response.put("data", Map.of("complaint", updatedComplaint));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update complaint status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComplaint(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Optional<Complaint> complaintOpt = complaintService.getComplaintById(id);
            if (complaintOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Complaint not found"));
            }
            Complaint complaint = complaintOpt.get();

            boolean isAdmin = "ADMIN".equals(userRole);
            boolean isOwner = "STUDENT".equals(userRole)
                    && userId != null && userId.equals(complaint.getReportedBy());

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "You can only delete your own complaints"));
            }
            if (!isAdmin && !"PENDING".equals(complaint.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Only pending complaints can be deleted"));
            }

            complaintService.deleteComplaint(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to delete complaint: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getComplaintStats(HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can view stats
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> stats = complaintService.getComplaintStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch complaint stats: " + e.getMessage()));
        }
    }
}