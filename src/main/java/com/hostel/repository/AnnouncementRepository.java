package com.hostel.repository;

import com.hostel.model.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
    
    List<Announcement> findByCreatedBy(String createdBy);
    
    List<Announcement> findByStatus(String status);
    
    List<Announcement> findByType(String type);
    
    List<Announcement> findByPriority(String priority);
    
    List<Announcement> findByTargetAudience(String targetAudience);
    
    @Query("{ 'status': 'PUBLISHED', '$or': [ { 'expiryDate': { '$exists': false } }, { 'expiryDate': { '$gte': ?0 } } ] }")
    List<Announcement> findActiveAnnouncements(LocalDateTime currentDate);
    
    @Query("{ 'status': 'PUBLISHED', 'targetAudience': { '$in': ['ALL', 'STUDENTS'] }, '$or': [ { 'expiryDate': { '$exists': false } }, { 'expiryDate': { '$gte': ?0 } } ] }")
    Page<Announcement> findActiveAnnouncementsForStudents(LocalDateTime currentDate, Pageable pageable);
    
    @Query("{ 'status': 'PUBLISHED', 'targetUsers': ?0, '$or': [ { 'expiryDate': { '$exists': false } }, { 'expiryDate': { '$gte': ?1 } } ] }")
    List<Announcement> findActiveAnnouncementsForUser(String userId, LocalDateTime currentDate);
    
    @Query("{ 'isSticky': true, 'status': 'PUBLISHED' }")
    List<Announcement> findStickyAnnouncements();
    
    long countByStatus(String status);
    
    long countByType(String type);
    
    long countByPriority(String priority);
    
    @Query("{ 'readBy.userId': ?0 }")
    List<Announcement> findReadByUser(String userId);
    
    @Query("{ 'likes.userId': ?0 }")
    List<Announcement> findLikedByUser(String userId);
}