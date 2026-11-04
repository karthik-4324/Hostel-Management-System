package com.hostel.repository;

import com.hostel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
    List<Room> findByIsActive(boolean isActive);
    
    List<Room> findByStatus(String status);
    
    List<Room> findByBlock(String block);
    
    List<Room> findByFloor(Integer floor);
    
    List<Room> findByType(String type);
    
    List<Room> findByBlockAndFloor(String block, Integer floor);
    
    @Query("{ 'isActive': true, 'status': 'AVAILABLE', 'currentOccupancy': { '$lt': '$capacity' } }")
    List<Room> findAvailableRooms();
    
    @Query("{ 'isActive': true, 'status': 'AVAILABLE', 'currentOccupancy': { '$lt': '$capacity' }, 'type': ?0 }")
    List<Room> findAvailableRoomsByType(String type);
    
    @Query("{ 'isActive': true, 'status': 'AVAILABLE', 'currentOccupancy': { '$lt': '$capacity' }, 'block': ?0 }")
    List<Room> findAvailableRoomsByBlock(String block);
    
    long countByIsActive(boolean isActive);
    
    long countByStatus(String status);
    
    long countByStatusAndIsActive(String status, boolean isActive);
    
    @Query("{ 'occupants.studentId': ?0 }")
    Optional<Room> findByOccupantStudentId(String studentId);
    
    boolean existsByRoomNumber(String roomNumber);
}