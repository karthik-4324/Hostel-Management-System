package com.hostel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "fees")
public class Fee {
    @Id
    private String id;
    
    @NotNull(message = "Student ID is required")
    private String studentId;
    
    private String feeType; // room_rent, mess_fee, security_deposit, maintenance, electricity, water, internet, other
    
    @Min(value = 0, message = "Amount cannot be negative")
    private Double amount;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    private LocalDate paidDate;
    
    private String status = "PENDING"; // PENDING, PAID, OVERDUE, PARTIAL, WAIVED
    
    private String paymentMethod; // cash, card, upi, bank_transfer, cheque, online
    
    private String transactionId;
    
    private String receiptNumber;
    
    @Min(value = 1, message = "Month must be between 1 and 12")
    private Integer month;
    
    private Integer year;
    
    private String description;
    
    @Min(value = 0, message = "Late fee cannot be negative")
    private Double lateFee = 0.0;
    
    @Min(value = 0, message = "Discount cannot be negative")
    private Double discount = 0.0;
    
    private Double finalAmount;
    
    @Min(value = 0, message = "Paid amount cannot be negative")
    private Double paidAmount = 0.0;
    
    private Double balanceAmount = 0.0;
    
    private List<PaymentHistory> paymentHistory = new ArrayList<>();
    
    private List<Reminder> reminders = new ArrayList<>();
    
    private String roomId;
    
    private String createdBy;
    
    private String updatedBy;
    
    private String notes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Fee() {}
    
    public Fee(String studentId, String feeType, Double amount, LocalDate dueDate, Integer month, Integer year) {
        this.studentId = studentId;
        this.feeType = feeType;
        this.amount = amount;
        this.dueDate = dueDate;
        this.month = month;
        this.year = year;
        calculateFinalAmount();
    }
    
    public void calculateFinalAmount() {
        this.finalAmount = this.amount + this.lateFee - this.discount;
        this.balanceAmount = this.finalAmount - this.paidAmount;
        
        // Update status based on payment
        if (this.paidAmount == 0) {
            this.status = LocalDate.now().isAfter(this.dueDate) ? "OVERDUE" : "PENDING";
        } else if (this.paidAmount >= this.finalAmount) {
            this.status = "PAID";
            if (this.paidDate == null) {
                this.paidDate = LocalDate.now();
            }
        } else {
            this.status = "PARTIAL";
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { 
        this.amount = amount;
        calculateFinalAmount();
    }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getLateFee() { return lateFee; }
    public void setLateFee(Double lateFee) { 
        this.lateFee = lateFee;
        calculateFinalAmount();
    }
    
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { 
        this.discount = discount;
        calculateFinalAmount();
    }
    
    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }
    
    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { 
        this.paidAmount = paidAmount;
        calculateFinalAmount();
    }
    
    public Double getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(Double balanceAmount) { this.balanceAmount = balanceAmount; }
    
    public List<PaymentHistory> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(List<PaymentHistory> paymentHistory) { this.paymentHistory = paymentHistory; }
    
    public List<Reminder> getReminders() { return reminders; }
    public void setReminders(List<Reminder> reminders) { this.reminders = reminders; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public long getDaysOverdue() {
        if ("OVERDUE".equals(status) || ("PENDING".equals(status) && LocalDate.now().isAfter(dueDate))) {
            return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        }
        return 0;
    }
    
    public double getPaymentPercentage() {
        return finalAmount > 0 ? (paidAmount / finalAmount) * 100 : 100;
    }
    
    // Nested classes
    public static class PaymentHistory {
        private Double amount;
        private LocalDate paidDate;
        private String paymentMethod;
        private String transactionId;
        private String receiptNumber;
        private String paidBy;
        
        public PaymentHistory() {}
        
        public PaymentHistory(Double amount, String paymentMethod, String paidBy) {
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.paidBy = paidBy;
            this.paidDate = LocalDate.now();
        }
        
        // Getters and Setters
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public LocalDate getPaidDate() { return paidDate; }
        public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getReceiptNumber() { return receiptNumber; }
        public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
        
        public String getPaidBy() { return paidBy; }
        public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
    }
    
    public static class Reminder {
        private LocalDateTime sentDate;
        private String type; // email, sms, notification
        private String status; // sent, delivered, failed
        
        public Reminder() {}
        
        public Reminder(String type, String status) {
            this.type = type;
            this.status = status;
            this.sentDate = LocalDateTime.now();
        }
        
        // Getters and Setters
        public LocalDateTime getSentDate() { return sentDate; }
        public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}