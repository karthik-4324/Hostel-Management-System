package com.hostel.repository;

import com.hostel.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByStudentId(String studentId);
    
    Optional<User> findByEmployeeId(String employeeId);
    
    List<User> findByRole(String role);
    
    Page<User> findByRoleAndIsActive(String role, boolean isActive, Pageable pageable);
    
    List<User> findByRoleAndIsActive(String role, boolean isActive);
    
    @Query("{ 'role': ?0, 'isActive': true, '$or': [ " +
           "{ 'firstName': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'lastName': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'studentId': { '$regex': ?1, '$options': 'i' } } ] }")
    Page<User> findByRoleAndSearch(String role, String searchTerm, Pageable pageable);
    
    @Query("{ 'role': ?0, 'isActive': true, '$or': [ " +
           "{ 'firstName': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'lastName': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'studentId': { '$regex': ?1, '$options': 'i' } } ] }")
    List<User> findByRoleAndSearch(String role, String searchTerm);
    
    long countByRole(String role);
    
    long countByRoleAndIsActive(String role, boolean isActive);
    
    List<User> findByRoomId(String roomId);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentId(String studentId);
    
    boolean existsByEmployeeId(String employeeId);
}