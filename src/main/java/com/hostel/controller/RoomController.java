package com.hostel.controller;

import com.hostel.model.Room;
import com.hostel.service.RoomService;
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
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    private boolean isStaff() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return false;
        }
        Object role = attrs.getRequest().getAttribute("userRole");
        return "ADMIN".equals(role) || "WARDEN".equals(role);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String block) {
        try {
            if (!isStaff()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }
            Map<String, Object> result = roomService.getAllRooms(page, limit, search, status, block);
            ;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch rooms: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoom(@PathVariable String id) {
        try {
            if (!isStaff()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }
            Room room = roomService.getRoomById(id);

            if (room == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Room not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("room", room));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch room: " + e.getMessage()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createRoom(@Valid @RequestBody Room room, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can create rooms
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Room savedRoom = roomService.createRoom(room);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room created successfully");
            response.put("data", Map.of("room", savedRoom));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to create room: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable String id, @Valid @RequestBody Room roomDetails,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can update rooms
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Room updatedRoom = roomService.updateRoom(id, roomDetails);

            if (updatedRoom == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Room not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room updated successfully");
            response.put("data", Map.of("room", updatedRoom));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update room: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable String id, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin can delete rooms
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            roomService.deleteRoom(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to delete room: " + e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String block) {
        try {
            if (!isStaff()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }
            Map<String, Object> result = roomService.getAvailableRooms(type, block);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch available rooms: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getRoomStats() {
        try {
            if (!isStaff()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied"));
            }
            Map<String, Object> stats = roomService.getRoomStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch room stats: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignStudent(@PathVariable String id, @RequestBody Map<String, Object> assignmentData,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can assign students
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            String studentId = (String) assignmentData.get("studentId");
            Object bed = assignmentData.get("bedNumber");
            Integer bedNumber = bed == null ? null : Integer.valueOf(bed.toString());

            Room updatedRoom = roomService.assignStudent(id, studentId, bedNumber);

            if (updatedRoom == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Failed to assign student to room"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student assigned to room successfully");
            response.put("data", Map.of("room", updatedRoom));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to assign student: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/remove")
    public ResponseEntity<?> removeStudent(@PathVariable String id, @RequestBody Map<String, String> removalData,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can remove students
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            String studentId = removalData.get("studentId");

            Room updatedRoom = roomService.removeStudent(id, studentId);

            if (updatedRoom == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Failed to remove student from room"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student removed from room successfully");
            response.put("data", Map.of("room", updatedRoom));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to remove student: " + e.getMessage()));
        }
    }
}