package com.hostel.service;

import com.hostel.model.Announcement;
import com.hostel.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getAllAnnouncements(String userId, String userRole, int page, int limit, String search, String type, String priority) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        Query query = new Query();
        
        // Filter based on user role and target audience
        if ("STUDENT".equals(userRole)) {
            query.addCriteria(Criteria.where("status").is("PUBLISHED")
                .and("targetAudience").in("ALL", "STUDENTS"));
            
            // Check expiry date
            Criteria expiryCriteria = new Criteria().orOperator(
                Criteria.where("expiryDate").exists(false),
                Criteria.where("expiryDate").gte(LocalDateTime.now())
            );
            query.addCriteria(expiryCriteria);
        }
        
        // Add search criteria
        if (search != null && !search.trim().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        // Add type filter
        if (type != null && !type.trim().isEmpty()) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        
        // Add priority filter
        if (priority != null && !priority.trim().isEmpty()) {
            query.addCriteria(Criteria.where("priority").is(priority));
        }
        
        query.with(pageable);
        
        List<Announcement> announcements = mongoTemplate.find(query, Announcement.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Announcement.class);
        
        Map<String, Object> result = new HashMap<>();
        result.put("announcements", announcements);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        pagination.put("total", total);
        result.put("pagination", pagination);
        
        return result;
    }
    
    public Announcement getAnnouncementById(String id) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(id);
        return optionalAnnouncement.orElse(null);
    }
    
    public Announcement createAnnouncement(Announcement announcement) {
        announcement.setPublishDate(LocalDateTime.now());
        announcement.setStatus("PUBLISHED");
        return announcementRepository.save(announcement);
    }
    
    public Announcement updateAnnouncement(String id, Announcement announcementDetails) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(id);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            announcement.setTitle(announcementDetails.getTitle());
            announcement.setContent(announcementDetails.getContent());
            announcement.setType(announcementDetails.getType());
            announcement.setPriority(announcementDetails.getPriority());
            announcement.setTargetAudience(announcementDetails.getTargetAudience());
            announcement.setExpiryDate(announcementDetails.getExpiryDate());
            announcement.setSticky(announcementDetails.isSticky());
            return announcementRepository.save(announcement);
        }
        return null;
    }
    
    public void deleteAnnouncement(String id) {
        announcementRepository.deleteById(id);
    }
    
    public Announcement toggleLike(String announcementId, String userId) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            
            // Check if user already liked
            boolean alreadyLiked = announcement.getLikes().stream()
                .anyMatch(like -> like.getUserId().equals(userId));
            
            if (alreadyLiked) {
                announcement.getLikes().removeIf(like -> like.getUserId().equals(userId));
            } else {
                Announcement.Like like = new Announcement.Like(userId);
                announcement.getLikes().add(like);
            }
            
            return announcementRepository.save(announcement);
        }
        return null;
    }
    
    public Announcement addComment(String announcementId, String userId, String comment) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            
            Announcement.Comment newComment = new Announcement.Comment(userId, comment);
            announcement.getComments().add(newComment);
            
            return announcementRepository.save(announcement);
        }
        return null;
    }
    
    public Announcement markAsRead(String announcementId, String userId) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            
            // Check if already read
            boolean alreadyRead = announcement.getReadBy().stream()
                .anyMatch(read -> read.getUserId().equals(userId));
            
            if (!alreadyRead) {
                Announcement.ReadBy readBy = new Announcement.ReadBy(userId);
                announcement.getReadBy().add(readBy);
                announcement.setViewCount(announcement.getViewCount() + 1);
                return announcementRepository.save(announcement);
            }
        }
        return null;
    }
    
    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now());
    }
    
    public List<Announcement> getStickyAnnouncements() {
        return announcementRepository.findStickyAnnouncements();
    }
    
    public Map<String, Object> getAnnouncementStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", announcementRepository.count());
        stats.put("published", announcementRepository.countByStatus("PUBLISHED"));
        stats.put("draft", announcementRepository.countByStatus("DRAFT"));
        stats.put("expired", announcementRepository.countByStatus("EXPIRED"));
        
        return stats;
    }
}