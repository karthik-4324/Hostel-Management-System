package com.hostel.repository;

import com.hostel.model.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends MongoRepository<Leave, String> {
    
    List<Leave> findByStudentId(String studentId);
    
    Page<Leave> findByStudentId(String studentId, Pageable pageable);
    
    List<Leave> findByStatus(String status);
    
    List<Leave> findByLeaveType(String leaveType);
    
    List<Leave> findByStudentIdAndStatus(String studentId, String status);
    
    @Query("{ 'status': 'APPROVED', 'startDate': { '$lte': ?0 }, 'endDate': { '$gte': ?0 } }")
    List<Leave> findActiveLeaves(LocalDate currentDate);
    
    @Query("{ 'status': 'APPROVED', 'endDate': { '$lt': ?0 }, 'actualReturnDate': { '$exists': false } }")
    List<Leave> findOverdueLeaves(LocalDate currentDate);
    
    @Query("{ 'startDate': { '$gte': ?0, '$lte': ?1 } }")
    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    long countByStatus(String status);
    
    long countByStudentIdAndStatus(String studentId, String status);
    
    long countByLeaveType(String leaveType);
    
    @Query("{ '$expr': { '$eq': [{ '$year': '$startDate' }, ?0] } }")
    List<Leave> findByYear(Integer year);
}