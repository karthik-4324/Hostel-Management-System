package com.hostel.service;

import com.hostel.model.Announcement;
import com.hostel.repository.AnnouncementRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private void attachAuthorName(Announcement announcement) {
        if (announcement.getCreatedBy() != null) {
            userRepository.findById(announcement.getCreatedBy()).ifPresent(
                    u -> announcement.setAuthorName(u.getFirstName() + " " + u.getLastName()));
        }
    }

    public Map<String, Object> getAllAnnouncements(String userId, String userRole, int page, int limit,
            String search, String type, String priority) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Query query = new Query();
        List<Criteria> conditions = new ArrayList<>();

        // Students and wardens only see published, unexpired announcements meant for them
        if ("STUDENT".equals(userRole) || "WARDEN".equals(userRole)) {
            String audience = "STUDENT".equals(userRole) ? "STUDENTS" : "WARDENS";
            conditions.add(Criteria.where("status").is("PUBLISHED"));
            conditions.add(Criteria.where("targetAudience").in("ALL", audience));
            conditions.add(new Criteria().orOperator(
                    Criteria.where("expiryDate").exists(false),
                    Criteria.where("expiryDate").gte(LocalDateTime.now())));
        }

        if (search != null && !search.trim().isEmpty()) {
            String escaped = Pattern.quote(search.trim());
            conditions.add(new Criteria().orOperator(
                    Criteria.where("title").regex(escaped, "i"),
                    Criteria.where("content").regex(escaped, "i")));
        }

        if (type != null && !type.trim().isEmpty()) {
            conditions.add(Criteria.where("type").is(type.trim().toUpperCase()));
        }

        if (priority != null && !priority.trim().isEmpty()) {
            conditions.add(Criteria.where("priority").is(priority.trim().toUpperCase()));
        }

        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        }

        query.with(pageable);

        List<Announcement> announcements = mongoTemplate.find(query, Announcement.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Announcement.class);
        announcements.forEach(this::attachAuthorName);

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
        Announcement announcement = optionalAnnouncement.orElse(null);
        if (announcement != null) {
            attachAuthorName(announcement);
        }
        return announcement;
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

            boolean alreadyLiked = announcement.getLikes().stream()
                    .anyMatch(like -> like.getUserId().equals(userId));

            if (alreadyLiked) {
                announcement.getLikes().removeIf(like -> like.getUserId().equals(userId));
            } else {
                announcement.getLikes().add(new Announcement.Like(userId));
            }

            return announcementRepository.save(announcement);
        }
        return null;
    }

    public Announcement addComment(String announcementId, String userId, String comment) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            announcement.getComments().add(new Announcement.Comment(userId, comment));
            return announcementRepository.save(announcement);
        }
        return null;
    }

    public Announcement markAsRead(String announcementId, String userId) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();

            boolean alreadyRead = announcement.getReadBy().stream()
                    .anyMatch(read -> read.getUserId().equals(userId));

            if (!alreadyRead) {
                announcement.getReadBy().add(new Announcement.ReadBy(userId));
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