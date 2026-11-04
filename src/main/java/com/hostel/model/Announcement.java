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

@Document(collection = "announcements")
public class Announcement {
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;
    
    private String type = "GENERAL"; // GENERAL, URGENT, EVENT, MAINTENANCE, FEE, ACADEMIC, HOLIDAY
    
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    
    private String targetAudience = "ALL"; // ALL, STUDENTS, WARDENS, ADMINS, SPECIFIC_ROOMS, SPECIFIC_USERS
    
    private List<String> targetRooms = new ArrayList<>();
    
    private List<String> targetUsers = new ArrayList<>();
    
    private String createdBy;
    
    private LocalDateTime publishDate;
    
    private LocalDateTime expiryDate;
    
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED, EXPIRED
    
    private List<Attachment> attachments = new ArrayList<>();
    
    private List<ReadBy> readBy = new ArrayList<>();
    
    private List<Like> likes = new ArrayList<>();
    
    private List<Comment> comments = new ArrayList<>();
    
    private List<String> tags = new ArrayList<>();
    
    private boolean isSticky = false;
    
    private boolean emailSent = false;
    
    private boolean smsSent = false;
    
    private boolean notificationSent = false;
    
    private Integer viewCount = 0;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Announcement() {}
    
    public Announcement(String title, String content, String createdBy) {
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.publishDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    
    public List<String> getTargetRooms() { return targetRooms; }
    public void setTargetRooms(List<String> targetRooms) { this.targetRooms = targetRooms; }
    
    public List<String> getTargetUsers() { return targetUsers; }
    public void setTargetUsers(List<String> targetUsers) { this.targetUsers = targetUsers; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDateTime publishDate) { this.publishDate = publishDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    
    public List<ReadBy> getReadBy() { return readBy; }
    public void setReadBy(List<ReadBy> readBy) { this.readBy = readBy; }
    
    public List<Like> getLikes() { return likes; }
    public void setLikes(List<Like> likes) { this.likes = likes; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public boolean isSticky() { return isSticky; }
    public void setSticky(boolean sticky) { isSticky = sticky; }
    
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    
    public boolean isSmsSent() { return smsSent; }
    public void setSmsSent(boolean smsSent) { this.smsSent = smsSent; }
    
    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }
    
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "PUBLISHED".equals(status) && 
               (expiryDate == null || now.isBefore(expiryDate));
    }
    
    // Nested classes
    public static class Attachment {
        private String fileName;
        private String filePath;
        private Long fileSize;
        private LocalDateTime uploadDate;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getUploadDate() { return uploadDate; }
        public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    }
    
    public static class ReadBy {
        private String userId;
        private LocalDateTime readAt;
        
        public ReadBy() {}
        
        public ReadBy(String userId) {
            this.userId = userId;
            this.readAt = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getReadAt() { return readAt; }
        public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    }
    
    public static class Like {
        private String userId;
        private LocalDateTime likedAt;
        
        public Like() {}
        
        public Like(String userId) {
            this.userId = userId;
            this.likedAt = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getLikedAt() { return likedAt; }
        public void setLikedAt(LocalDateTime likedAt) { this.likedAt = likedAt; }
    }
    
    public static class Comment {
        private String userId;
        private String comment;
        private LocalDateTime commentedAt;
        
        public Comment() {}
        
        public Comment(String userId, String comment) {
            this.userId = userId;
            this.comment = comment;
            this.commentedAt = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public LocalDateTime getCommentedAt() { return commentedAt; }
        public void setCommentedAt(LocalDateTime commentedAt) { this.commentedAt = commentedAt; }
    }
}