package com.hostel.service;

import com.hostel.model.User;
import com.hostel.model.Room;
import com.hostel.repository.UserRepository;
import com.hostel.repository.RoomRepository;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            RoomRepository roomRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if ("STUDENT".equals(user.getRole())) {
            user.setStudentId(generateStudentId());
        } else {
            user.setEmployeeId(generateEmployeeId());
        }

        if ("ADMIN".equals(user.getRole()) || "WARDEN".equals(user.getRole())) {
            user.setJoinDate(LocalDate.now());
        }

        return userRepository.save(user);
    }

    public User updateUser(String id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setEmergencyContact(userDetails.getEmergencyContact());
            user.setCourse(userDetails.getCourse());
            user.setYear(userDetails.getYear());
            user.setDepartment(userDetails.getDepartment());
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(String id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            String roomId = optionalUser.get().getRoomId();
            if (roomId != null && !roomId.isEmpty()) {
                roomRepository.findById(roomId).ifPresent(room -> {
                    room.getOccupants().removeIf(o -> id.equals(o.getStudentId()));
                    room.setCurrentOccupancy(room.getOccupants().size());
                    if ("OCCUPIED".equals(room.getStatus())
                            && room.getCurrentOccupancy() < room.getCapacity()) {
                        room.setStatus("AVAILABLE");
                    }
                    roomRepository.save(room);
                });
            }
        }
        userRepository.deleteById(id);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<User> getActiveUsersByRole(String role) {
        return userRepository.findByRoleAndIsActive(role, true);
    }

    public List<User> searchUsers(String role, String searchTerm) {
        return userRepository.findByRoleAndSearch(role, searchTerm);
    }

    public long countUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    public long countActiveUsersByRole(String role) {
        return userRepository.countByRoleAndIsActive(role, true);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateLastLogin(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    public void attachRoomNumber(User student) {
        if (student.getRoomId() != null && !student.getRoomId().isEmpty()) {
            roomRepository.findById(student.getRoomId())
                    .ifPresent(r -> student.setRoomNumber(r.getRoomNumber()));
        }
    }

    public User deactivateUser(String id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActive(false);
            return userRepository.save(user);
        }
        return null;
    }

    public Map<String, Object> getStudentsWithPagination(int page, int limit, String search) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("firstName"));

        Page<User> studentsPage;
        if (search != null && !search.trim().isEmpty()) {
            studentsPage = userRepository.findByRoleAndSearch("STUDENT", search, pageable);
        } else {
            studentsPage = userRepository.findByRoleAndIsActive("STUDENT", true, pageable);
        }

        Map<String, Object> result = new HashMap<>();
        List<User> students = studentsPage.getContent();
        students.forEach(this::attachRoomNumber);
        result.put("students", students);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", studentsPage.getTotalPages());
        pagination.put("total", studentsPage.getTotalElements());
        result.put("pagination", pagination);

        return result;
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean assignStudentToRoom(String studentId, String roomId, Integer bedNumber) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (user.getRoomId() != null && !user.getRoomId().isEmpty()) {
            throw new RuntimeException("Student already has a room");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new RuntimeException("Room is at full capacity");
        }

        if (bedNumber == null || bedNumber < 1 || bedNumber > room.getCapacity()) {
            throw new RuntimeException("Bed number must be between 1 and " + room.getCapacity());
        }

        boolean bedTaken = room.getOccupants().stream()
                .anyMatch(o -> Objects.equals(o.getBedNumber(), bedNumber));
        if (bedTaken) {
            throw new RuntimeException("Bed " + bedNumber + " is already taken");
        }

        room.getOccupants().add(new Room.Occupant(studentId, bedNumber));
        room.setCurrentOccupancy(room.getOccupants().size());
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            room.setStatus("OCCUPIED");
        }
        roomRepository.save(room);

        user.setRoomId(roomId);
        userRepository.save(user);
        return true;
    }

    private String generateStudentId() {
        int year = LocalDateTime.now().getYear();
        int random = (int) (Math.random() * 10000);
        return String.format("STU%d%04d", year, random);
    }

    private String generateEmployeeId() {
        int random = (int) (Math.random() * 10000);
        return String.format("EMP%04d", random);
    }
}
