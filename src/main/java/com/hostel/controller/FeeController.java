package com.hostel.controller;

import com.hostel.model.Fee;
import com.hostel.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
@CrossOrigin(origins = "*")
public class FeeController {

    @Autowired
    private FeeService feeService;

    @GetMapping("")
    public ResponseEntity<?> getAllFees(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String feeType) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Map<String, Object> result = feeService.getAllFees(userId, userRole, page, limit, search, status, feeType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch fees: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFee(@PathVariable String id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String userRole = (String) request.getAttribute("userRole");

            Fee fee = feeService.getFeeById(id);

            if (fee == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Fee not found"));
            }

            // Check permissions for students
            if ("STUDENT".equals(userRole) && !fee.getStudentId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("fee", fee));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch fee: " + e.getMessage()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createFee(@Valid @RequestBody Fee fee, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            String userId = (String) request.getAttribute("userId");

            // Only admin and warden can create fees
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            fee.setCreatedBy(userId);
            Fee savedFee = feeService.createFee(fee);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fee created successfully");
            response.put("data", Map.of("fee", savedFee));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to create fee: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFee(@PathVariable String id, @Valid @RequestBody Fee feeDetails,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            String userId = (String) request.getAttribute("userId");

            // Only admin and warden can update fees
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            feeDetails.setUpdatedBy(userId);
            Fee updatedFee = feeService.updateFee(id, feeDetails);

            if (updatedFee == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Fee not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fee updated successfully");
            response.put("data", Map.of("fee", updatedFee));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update fee: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<?> addPayment(@PathVariable String id, @RequestBody Map<String, Object> paymentData,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");
            String userId = (String) request.getAttribute("userId");

            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Only the admin can record payments"));
            }

            Double amount = Double.valueOf(paymentData.get("amount").toString());
            String paymentMethod = (String) paymentData.get("paymentMethod");
            String transactionId = (String) paymentData.get("transactionId");

            Fee.PaymentHistory payment = new Fee.PaymentHistory(amount, paymentMethod, userId);
            payment.setTransactionId(transactionId);

            Fee updatedFee = feeService.addPayment(id, payment);

            if (updatedFee == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Fee not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment added successfully");
            response.put("data", Map.of("fee", updatedFee));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to add payment: " + e.getMessage()));
        }
    }

        @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFee(@PathVariable String id, HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Only the admin can delete fees"));
            }

            feeService.deleteFee(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fee deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to delete fee: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getFeeStats(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can view stats
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> stats = feeService.getFeeStats(year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch fee stats: " + e.getMessage()));
        }
    }

    @GetMapping("/defaulters")
    public ResponseEntity<?> getDefaulters(HttpServletRequest request) {
        try {
            String userRole = (String) request.getAttribute("userRole");

            // Only admin and warden can view defaulters
            if (!"ADMIN".equals(userRole) && !"WARDEN".equals(userRole)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Access denied"));
            }

            Map<String, Object> result = feeService.getDefaulters();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to fetch defaulters: " + e.getMessage()));
        }
    }
}