package com.hostel.repository;

import com.hostel.model.Fee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeeRepository extends MongoRepository<Fee, String> {
    
    List<Fee> findByStudentId(String studentId);
    
    Page<Fee> findByStudentId(String studentId, Pageable pageable);
    
    List<Fee> findByStatus(String status);
    
    List<Fee> findByFeeType(String feeType);
    
    List<Fee> findByMonthAndYear(Integer month, Integer year);
    
    List<Fee> findByYear(Integer year);
    
    List<Fee> findByStudentIdAndStatus(String studentId, String status);
    
    List<Fee> findByStudentIdAndYear(String studentId, Integer year);
    
    @Query("{ 'status': { '$in': ['OVERDUE', 'PARTIAL'] }, 'balanceAmount': { '$gt': 0 } }")
    List<Fee> findDefaulters();
    
    @Query("{ 'dueDate': { '$lt': ?0 }, 'status': { '$in': ['PENDING', 'PARTIAL'] } }")
    List<Fee> findOverdueFees(LocalDate currentDate);
    
    long countByStatus(String status);
    
    long countByStudentIdAndStatus(String studentId, String status);
    
    long countByYear(Integer year);
    
    @Query(value = "{ 'status': 'PAID', 'year': ?0 }")
    List<Fee> findPaidFeesByYear(Integer year);
    
    @Query(value = "{ 'status': { '$in': ['PENDING', 'OVERDUE', 'PARTIAL'] }, 'year': ?0 }")
    List<Fee> findPendingFeesByYear(Integer year);
    
    @Query(value = "{ 'studentId': ?0, 'status': { '$in': ['PENDING', 'OVERDUE', 'PARTIAL'] } }")
    List<Fee> findStudentPendingFeesAmounts(String studentId);
}