package com.hostel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "complaints")
public class Complaint {
    @Id
    private String id;
    
    private String complaintId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private String category; // plumbing, electrical, cleaning, maintenance, security, food, internet, other
    
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT
    
    private String status = "PENDING"; // PENDING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED
    
    private String reportedBy;
    
    private String assignedTo;
    
    private String roomId;
    
    private String location;
    
    private List<String> images = new ArrayList<>();
    
    private List<StatusHistory> statusHistory = new ArrayList<>();
    
    private Resolution resolution;
    
    private LocalDateTime expectedResolutionDate;
    
    private LocalDateTime actualResolutionDate;
    
    private boolean isUrgent = false;
    
    private List<String> tags = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Complaint() {}
    
    public Complaint(String title, String description, String category, String reportedBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.reportedBy = reportedBy;
        generateComplaintId();
    }
    
    private void generateComplaintId() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        this.complaintId = "CMP" + year + month + random;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { 
        this.priority = priority;
        this.isUrgent = "URGENT".equals(priority);
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    
    public List<StatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistory> statusHistory) { this.statusHistory = statusHistory; }
    
    public Resolution getResolution() { return resolution; }
    public void setResolution(Resolution resolution) { this.resolution = resolution; }
    
    public LocalDateTime getExpectedResolutionDate() { return expectedResolutionDate; }
    public void setExpectedResolutionDate(LocalDateTime expectedResolutionDate) { this.expectedResolutionDate = expectedResolutionDate; }
    
    public LocalDateTime getActualResolutionDate() { return actualResolutionDate; }
    public void setActualResolutionDate(LocalDateTime actualResolutionDate) { this.actualResolutionDate = actualResolutionDate; }
    
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isOverdue() {
        return expectedResolutionDate != null && 
               !"RESOLVED".equals(status) && 
               !"CLOSED".equals(status) && 
               LocalDateTime.now().isAfter(expectedResolutionDate);
    }
    
    // Nested classes
    public static class StatusHistory {
        private String status;
        private String updatedBy;
        private LocalDateTime updatedAt;
        private String comment;
        
        public StatusHistory() {}
        
        public StatusHistory(String status, String updatedBy, String comment) {
            this.status = status;
            this.updatedBy = updatedBy;
            this.comment = comment;
            this.updatedAt = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    
    public static class Resolution {
        private String description;
        private String resolvedBy;
        private LocalDateTime resolvedAt;
        private Double cost;
        private Integer rating;
        private String feedback;
        
        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getResolvedBy() { return resolvedBy; }
        public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
        
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        
        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }
        
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}