package com.hostel.service;

import com.hostel.model.Complaint;
import com.hostel.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    public Map<String, Object> getAllComplaints(String userId, String userRole, int page, int limit, String status, String category, String priority) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        Page<Complaint> complaintsPage;
        
        // Filter based on user role
        if ("STUDENT".equals(userRole)) {
            complaintsPage = complaintRepository.findByReportedBy(userId, pageable);
        } else {
            complaintsPage = complaintRepository.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("complaints", complaintsPage.getContent());
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", complaintsPage.getTotalPages());
        pagination.put("total", complaintsPage.getTotalElements());
        result.put("pagination", pagination);
        
        return result;
    }
    
    public Optional<Complaint> getComplaintById(String id) {
        return complaintRepository.findById(id);
    }
    
    public Complaint createComplaint(Complaint complaint) {
        if (complaint.getComplaintId() == null) {
            complaint.setComplaintId(generateComplaintId());
        }
        return complaintRepository.save(complaint);
    }
    
    public Complaint updateComplaint(String id, Complaint complaintDetails) {
        Optional<Complaint> optionalComplaint = complaintRepository.findById(id);
        if (optionalComplaint.isPresent()) {
            Complaint complaint = optionalComplaint.get();
            complaint.setTitle(complaintDetails.getTitle());
            complaint.setDescription(complaintDetails.getDescription());
            complaint.setCategory(complaintDetails.getCategory());
            complaint.setPriority(complaintDetails.getPriority());
            complaint.setLocation(complaintDetails.getLocation());
            return complaintRepository.save(complaint);
        }
        return null;
    }
    
    public Complaint updateComplaintStatus(String id, String status, String updatedBy, String comment) {
        Optional<Complaint> optionalComplaint = complaintRepository.findById(id);
        if (optionalComplaint.isPresent()) {
            Complaint complaint = optionalComplaint.get();
            complaint.setStatus(status);
            
            // Add to status history
            Complaint.StatusHistory statusHistory = new Complaint.StatusHistory(status, updatedBy, comment);
            complaint.getStatusHistory().add(statusHistory);
            
            if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
                complaint.setActualResolutionDate(LocalDateTime.now());
            }
            
            return complaintRepository.save(complaint);
        }
        return null;
    }
    
    public void deleteComplaint(String id) {
        complaintRepository.deleteById(id);
    }
    
    public List<Complaint> getComplaintsByReportedBy(String reportedBy) {
        return complaintRepository.findByReportedBy(reportedBy);
    }
    
    public List<Complaint> getComplaintsByStatus(String status) {
        return complaintRepository.findByStatus(status);
    }
    
    public List<Complaint> getUrgentComplaints() {
        return complaintRepository.findUrgentComplaints();
    }
    
    public List<Complaint> getOverdueComplaints() {
        return complaintRepository.findOverdueComplaints(LocalDateTime.now());
    }
    
    public Map<String, Object> getComplaintStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", complaintRepository.count());
        stats.put("pending", complaintRepository.countByStatus("PENDING"));
        stats.put("inProgress", complaintRepository.countByStatus("IN_PROGRESS"));
        stats.put("resolved", complaintRepository.countByStatus("RESOLVED"));
        stats.put("urgent", complaintRepository.findUrgentComplaints().size());
        stats.put("overdue", complaintRepository.findOverdueComplaints(LocalDateTime.now()).size());
        
        return stats;
    }
    
    private String generateComplaintId() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "CMP" + year + month + random;
    }
}