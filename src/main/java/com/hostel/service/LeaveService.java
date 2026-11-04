package com.hostel.service;

import com.hostel.model.Leave;
import com.hostel.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getAllLeaves(String userId, String userRole, int page, int limit, String search, String status, String leaveType) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        Query query = new Query();
        
        // Filter based on user role
        if ("STUDENT".equals(userRole)) {
            query.addCriteria(Criteria.where("studentId").is(userId));
        }
        
        // Add search criteria (for admin/warden)
        if (search != null && !search.trim().isEmpty() && !"STUDENT".equals(userRole)) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("leaveId").regex(search, "i"),
                Criteria.where("reason").regex(search, "i"),
                Criteria.where("leaveType").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        // Add status filter
        if (status != null && !status.trim().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status.toUpperCase()));
        }
        
        // Add leave type filter
        if (leaveType != null && !leaveType.trim().isEmpty()) {
            query.addCriteria(Criteria.where("leaveType").is(leaveType));
        }
        
        query.with(pageable);
        
        List<Leave> leaves = mongoTemplate.find(query, Leave.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Leave.class);
        
        Map<String, Object> result = new HashMap<>();
        result.put("leaves", leaves);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        pagination.put("total", total);
        result.put("pagination", pagination);
        
        return result;
    }
    
    public Leave getLeaveById(String id) {
        Optional<Leave> optionalLeave = leaveRepository.findById(id);
        return optionalLeave.orElse(null);
    }
    
    public Leave createLeave(Leave leave) {
        if (leave.getLeaveId() == null) {
            leave.setLeaveId(generateLeaveId());
        }
        return leaveRepository.save(leave);
    }
    
    public Leave updateLeave(String id, Leave leaveDetails) {
        Optional<Leave> optionalLeave = leaveRepository.findById(id);
        if (optionalLeave.isPresent()) {
            Leave leave = optionalLeave.get();
            leave.setReason(leaveDetails.getReason());
            leave.setEmergencyContact(leaveDetails.getEmergencyContact());
            leave.setDestination(leaveDetails.getDestination());
            return leaveRepository.save(leave);
        }
        return null;
    }
    
    public Leave updateLeaveStatus(String id, String status, String updatedBy, String comment) {
        Optional<Leave> optionalLeave = leaveRepository.findById(id);
        if (optionalLeave.isPresent()) {
            Leave leave = optionalLeave.get();
            leave.setStatus(status);
            
            // Add to status history
            Leave.StatusHistory statusHistory = new Leave.StatusHistory(status, updatedBy, comment);
            leave.getStatusHistory().add(statusHistory);
            
            if ("APPROVED".equals(status)) {
                leave.setApprovedBy(updatedBy);
                leave.setApprovedDate(LocalDateTime.now());
            } else if ("REJECTED".equals(status)) {
                leave.setRejectionReason(comment);
            }
            
            return leaveRepository.save(leave);
        }
        return null;
    }
    
    public Leave requestExtension(String id, LocalDate newEndDate, String reason) {
        Optional<Leave> optionalLeave = leaveRepository.findById(id);
        if (optionalLeave.isPresent()) {
            Leave leave = optionalLeave.get();
            
            Leave.ExtensionRequest extensionRequest = new Leave.ExtensionRequest();
            extensionRequest.setRequestedEndDate(newEndDate);
            extensionRequest.setReason(reason);
            extensionRequest.setRequestedDate(LocalDateTime.now());
            extensionRequest.setStatus("PENDING");
            
            leave.getExtensionRequests().add(extensionRequest);
            return leaveRepository.save(leave);
        }
        return null;
    }
    
    public Leave markReturn(String id, LocalDate actualReturnDate) {
        Optional<Leave> optionalLeave = leaveRepository.findById(id);
        if (optionalLeave.isPresent()) {
            Leave leave = optionalLeave.get();
            leave.setActualReturnDate(actualReturnDate != null ? actualReturnDate : LocalDate.now());
            return leaveRepository.save(leave);
        }
        return null;
    }
    
    public void deleteLeave(String id) {
        leaveRepository.deleteById(id);
    }
    
    public List<Leave> getLeavesByStudentId(String studentId) {
        return leaveRepository.findByStudentId(studentId);
    }
    
    public List<Leave> getLeavesByStatus(String status) {
        return leaveRepository.findByStatus(status);
    }
    
    public List<Leave> getActiveLeaves() {
        return leaveRepository.findActiveLeaves(LocalDate.now());
    }
    
    public List<Leave> getOverdueLeaves() {
        return leaveRepository.findOverdueLeaves(LocalDate.now());
    }
    
    public Map<String, Object> getLeaveStats(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", leaveRepository.findByYear(year).size());
        stats.put("pending", leaveRepository.countByStatus("PENDING"));
        stats.put("approved", leaveRepository.countByStatus("APPROVED"));
        stats.put("rejected", leaveRepository.countByStatus("REJECTED"));
        stats.put("active", leaveRepository.findActiveLeaves(LocalDate.now()).size());
        stats.put("overdue", leaveRepository.findOverdueLeaves(LocalDate.now()).size());
        stats.put("year", year);
        
        return stats;
    }
    
    private String generateLeaveId() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "LV" + year + month + random;
    }
}