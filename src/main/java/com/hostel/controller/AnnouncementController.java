package com.hostel.controller;

import com.hostel.model.Announcement;
import com.hostel.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("")
    public ResponseEntity<?> getAllAnnouncements(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            Map<String, Object> result = announcementService.getAllAnnouncements(userId, userRole, page, limit, search, type, priority);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch announcements: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncement(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            Announcement announcement = announcementService.getAnnouncementById(id);
            
            if (announcement == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Announcement not found"));
            }
            
            // Mark as read
            announcementService.markAsRead(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("announcement", announcement));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch announcement: " + e.getMessage()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createAnnouncement(@Valid @RequestBody Announcement announcement, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can create announcements
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            announcement.setCreatedBy(userId);
            Announcement savedAnnouncement = announcementService.createAnnouncement(announcement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement created successfully");
            response.put("data", Map.of("announcement", savedAnnouncement));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to create announcement: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnouncement(@PathVariable String id, @Valid @RequestBody Announcement announcementDetails, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can update announcements
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            Announcement updatedAnnouncement = announcementService.updateAnnouncement(id, announcementDetails);
            
            if (updatedAnnouncement == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Announcement not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement updated successfully");
            response.put("data", Map.of("announcement", updatedAnnouncement));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update announcement: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable String id, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin can delete announcements
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            announcementService.deleteAnnouncement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to delete announcement: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            Announcement result = announcementService.toggleLike(id, userId);
            
            if (result == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Announcement not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("announcement", result));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to toggle like: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable String id, @RequestBody Map<String, String> commentData, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String comment = commentData.get("comment");
            
            Announcement result = announcementService.addComment(id, userId, comment);
            
            if (result == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Announcement not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment added successfully");
            response.put("data", Map.of("announcement", result));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to add comment: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAnnouncementStats(HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            
            // Only admin and warden can view stats
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            Map<String, Object> stats = announcementService.getAnnouncementStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch announcement stats: " + e.getMessage()));
        }
    }
}