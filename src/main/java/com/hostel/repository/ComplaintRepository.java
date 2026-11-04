package com.hostel.repository;

import com.hostel.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    
    List<Complaint> findByReportedBy(String reportedBy);
    
    Page<Complaint> findByReportedBy(String reportedBy, Pageable pageable);
    
    List<Complaint> findByAssignedTo(String assignedTo);
    
    List<Complaint> findByStatus(String status);
    
    List<Complaint> findByCategory(String category);
    
    List<Complaint> findByPriority(String priority);
    
    List<Complaint> findByRoomId(String roomId);
    
    List<Complaint> findByReportedByAndStatus(String reportedBy, String status);
    
    @Query("{ 'status': { '$in': ['PENDING', 'IN_PROGRESS'] }, 'expectedResolutionDate': { '$lt': ?0 } }")
    List<Complaint> findOverdueComplaints(LocalDateTime currentDate);
    
    @Query("{ 'priority': 'URGENT', 'status': { '$in': ['PENDING', 'IN_PROGRESS'] } }")
    List<Complaint> findUrgentComplaints();
    
    @Query("{ 'category': { '$in': ['plumbing', 'electrical', 'maintenance'] }, 'status': { '$in': ['PENDING', 'IN_PROGRESS'] } }")
    List<Complaint> findMaintenanceComplaints();
    
    long countByStatus(String status);
    
    long countByCategory(String category);
    
    long countByPriority(String priority);
    
    long countByReportedBy(String reportedBy);
    
    @Query("{ 'createdAt': { '$gte': ?0, '$lte': ?1 } }")
    List<Complaint> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}