package com.hostel.service;

import com.hostel.model.Room;
import com.hostel.repository.RoomRepository;
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
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getAllRooms(int page, int limit, String search, String status, String block) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("roomNumber"));

        Query query = new Query();
        query.addCriteria(Criteria.where("isActive").is(true));

        // Add search criteria
        if (search != null && !search.trim().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("roomNumber").regex(search, "i"),
                    Criteria.where("block").regex(search, "i"),
                    Criteria.where("type").regex(search, "i"));
            query.addCriteria(searchCriteria);
        }

        // Add status filter
        if (status != null && !status.trim().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status.toUpperCase()));
        }

        // Add block filter
        if (block != null && !block.trim().isEmpty()) {
            query.addCriteria(Criteria.where("block").is(block));
        }

        query.with(pageable);

        List<Room> rooms = mongoTemplate.find(query, Room.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Room.class);

        Map<String, Object> result = new HashMap<>();
        result.put("rooms", rooms);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        pagination.put("total", total);
        result.put("pagination", pagination);

        return result;
    }

    public Room getRoomById(String id) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        return optionalRoom.orElse(null);
    }

    public Room createRoom(Room room) {
        // Check if room number already exists
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Room number already exists");
        }

        room.setCurrentOccupancy(0);
        room.setStatus("AVAILABLE");
        return roomRepository.save(room);
    }

    public Room updateRoom(String id, Room roomDetails) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            room.setRoomNumber(roomDetails.getRoomNumber());
            room.setFloor(roomDetails.getFloor());
            room.setBlock(roomDetails.getBlock());
            room.setType(roomDetails.getType());
            room.setCapacity(roomDetails.getCapacity());
            room.setMonthlyRent(roomDetails.getMonthlyRent());
            room.setSecurityDeposit(roomDetails.getSecurityDeposit());
            room.setAmenities(roomDetails.getAmenities());
            room.setDescription(roomDetails.getDescription());
            return roomRepository.save(room);
        }
        return null;
    }

    public void deleteRoom(String id) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            if (room.getCurrentOccupancy() > 0) {
                throw new RuntimeException("Cannot delete a room that has students. Remove them first.");
            }
            room.setActive(false);
            roomRepository.save(room);
        }
    }

    public Room assignStudent(String roomId, String studentId, Integer bedNumber) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();

            if (room.getCurrentOccupancy() >= room.getCapacity()) {
                throw new RuntimeException("Room is at full capacity");
            }

            // Check if student is already in this room
            boolean alreadyAssigned = room.getOccupants().stream()
                    .anyMatch(occupant -> occupant.getStudentId().equals(studentId));

            if (alreadyAssigned) {
                throw new RuntimeException("Student is already assigned to this room");
            }

            Room.Occupant occupant = new Room.Occupant(studentId, bedNumber);
            room.getOccupants().add(occupant);
            room.setCurrentOccupancy(room.getOccupants().size());

            // Update room status
            if (room.getCurrentOccupancy() >= room.getCapacity()) {
                room.setStatus("OCCUPIED");
            }

            return roomRepository.save(room);
        }
        return null;
    }

    public Room removeStudent(String roomId, String studentId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();

            room.getOccupants().removeIf(occupant -> occupant.getStudentId().equals(studentId));
            room.setCurrentOccupancy(room.getOccupants().size());

            // Update room status
            if (room.getCurrentOccupancy() == 0) {
                room.setStatus("AVAILABLE");
            }

            return roomRepository.save(room);
        }
        return null;
    }

    public Map<String, Object> getAvailableRooms(String type, String block) {
        Query query = new Query();
        query.addCriteria(Criteria.where("isActive").is(true)
                .and("status").is("AVAILABLE"));

        if (type != null && !type.trim().isEmpty()) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        if (block != null && !block.trim().isEmpty()) {
            query.addCriteria(Criteria.where("block").is(block));
        }

        List<Room> rooms = mongoTemplate.find(query, Room.class).stream()
                .filter(r -> r.getCurrentOccupancy() < r.getCapacity())
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("rooms", rooms);
        result.put("total", rooms.size());

        return result;
    }

    public Map<String, Object> getRoomStats() {
        long total = roomRepository.countByIsActive(true);
        long occupied = roomRepository.countByStatusAndIsActive("OCCUPIED", true);
        long available = roomRepository.countByStatusAndIsActive("AVAILABLE", true);
        long maintenance = roomRepository.countByStatusAndIsActive("MAINTENANCE", true);

        double occupancyPercentage = total > 0 ? (double) occupied / total * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("occupied", occupied);
        stats.put("available", available);
        stats.put("maintenance", maintenance);
        stats.put("occupancyPercentage", Math.round(occupancyPercentage * 100.0) / 100.0);

        return stats;
    }

    public Optional<Room> findRoomByStudentId(String studentId) {
        return roomRepository.findByOccupantStudentId(studentId);
    }
}