package com.hostel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "leaves")
public class Leave {
    @Id
    private String id;
    
    private String leaveId;
    
    @NotNull(message = "Student ID is required")
    private String studentId;
    
    private String leaveType; // home, medical, emergency, personal, academic, other
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
    
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED
    
    private LocalDateTime appliedDate;
    
    private String approvedBy;
    
    private LocalDateTime approvedDate;
    
    private String rejectionReason;
    
    private EmergencyContact emergencyContact;
    
    private Destination destination;
    
    private List<String> attachments = new ArrayList<>();
    
    private LocalDate actualReturnDate;
    
    private boolean isExtended = false;
    
    private List<ExtensionRequest> extensionRequests = new ArrayList<>();
    
    private List<StatusHistory> statusHistory = new ArrayList<>();
    
    private ParentApproval parentApproval;
    
    private MedicalCertificate medicalCertificate;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Leave() {
        this.appliedDate = LocalDateTime.now();
        generateLeaveId();
    }
    
    public Leave(String studentId, String leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        this();
        this.studentId = studentId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }
    
    private void generateLeaveId() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        this.leaveId = "LV" + year + month + random;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(EmergencyContact emergencyContact) { this.emergencyContact = emergencyContact; }
    
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
    
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    
    public boolean isExtended() { return isExtended; }
    public void setExtended(boolean extended) { isExtended = extended; }
    
    public List<ExtensionRequest> getExtensionRequests() { return extensionRequests; }
    public void setExtensionRequests(List<ExtensionRequest> extensionRequests) { this.extensionRequests = extensionRequests; }
    
    public List<StatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistory> statusHistory) { this.statusHistory = statusHistory; }
    
    public ParentApproval getParentApproval() { return parentApproval; }
    public void setParentApproval(ParentApproval parentApproval) { this.parentApproval = parentApproval; }
    
    public MedicalCertificate getMedicalCertificate() { return medicalCertificate; }
    public void setMedicalCertificate(MedicalCertificate medicalCertificate) { this.medicalCertificate = medicalCertificate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public long getDurationDays() {
        return startDate != null && endDate != null ? ChronoUnit.DAYS.between(startDate, endDate) + 1 : 0;
    }
    
    public String getCurrentStatus() {
        LocalDate now = LocalDate.now();
        
        if (!"APPROVED".equals(status)) {
            return status;
        }
        
        if (now.isBefore(startDate)) {
            return "UPCOMING";
        } else if (!now.isAfter(endDate)) {
            return "ACTIVE";
        } else {
            return actualReturnDate != null ? "COMPLETED" : "OVERDUE";
        }
    }
    
    public long getOverdueDays() {
        if ("APPROVED".equals(status) && actualReturnDate == null) {
            LocalDate now = LocalDate.now();
            if (now.isAfter(endDate)) {
                return ChronoUnit.DAYS.between(endDate, now);
            }
        }
        return 0;
    }
    
    // Nested classes
    public static class EmergencyContact {
        private String name;
        private String phone;
        private String relation;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
    }
    
    public static class Destination {
        private String address;
        private String city;
        private String state;
        private String pincode;
        
        // Getters and Setters
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
    }
    
    public static class ExtensionRequest {
        private LocalDate requestedEndDate;
        private String reason;
        private LocalDateTime requestedDate;
        private String status = "PENDING";
        private String approvedBy;
        private LocalDateTime approvedDate;
        
        // Getters and Setters
        public LocalDate getRequestedEndDate() { return requestedEndDate; }
        public void setRequestedEndDate(LocalDate requestedEndDate) { this.requestedEndDate = requestedEndDate; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public LocalDateTime getRequestedDate() { return requestedDate; }
        public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        
        public LocalDateTime getApprovedDate() { return approvedDate; }
        public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
    }
    
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
    
    public static class ParentApproval {
        private boolean required = false;
        private boolean obtained = false;
        private String contactNumber;
        private LocalDateTime approvalDate;
        
        // Getters and Setters
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public boolean isObtained() { return obtained; }
        public void setObtained(boolean obtained) { this.obtained = obtained; }
        
        public String getContactNumber() { return contactNumber; }
        public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
        
        public LocalDateTime getApprovalDate() { return approvalDate; }
        public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
    }
    
    public static class MedicalCertificate {
        private boolean required = false;
        private boolean uploaded = false;
        private String fileName;
        private LocalDateTime uploadDate;
        
        // Getters and Setters
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public boolean isUploaded() { return uploaded; }
        public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public LocalDateTime getUploadDate() { return uploadDate; }
        public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    }
}