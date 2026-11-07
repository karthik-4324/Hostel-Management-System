package com.hostel.service;

import com.hostel.repository.*;
import com.hostel.model.User;
import com.hostel.model.Fee;
import com.hostel.model.Complaint;
import com.hostel.model.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Student statistics
        Map<String, Object> studentStats = new HashMap<>();
        studentStats.put("total", userRepository.countByRoleAndIsActive("STUDENT", true));
        studentStats.put("active", userRepository.countByRoleAndIsActive("STUDENT", true));
        stats.put("students", studentStats);

        // Room statistics
        Map<String, Object> roomStats = new HashMap<>();
        long totalRooms = roomRepository.countByIsActive(true);
        long occupiedRooms = roomRepository.countByStatusAndIsActive("OCCUPIED", true);
        roomStats.put("total", totalRooms);
        roomStats.put("occupied", occupiedRooms);
        roomStats.put("available", totalRooms - occupiedRooms);
        roomStats.put("occupancyRate", totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0);
        stats.put("rooms", roomStats);

        // Complaint statistics
        Map<String, Object> complaintStats = new HashMap<>();
        complaintStats.put("pending", complaintRepository.countByStatus("PENDING"));

        // Calculate overdue complaints
        List<Complaint> overdueComplaints = complaintRepository.findOverdueComplaints(LocalDateTime.now());
        complaintStats.put("overdue", overdueComplaints.size());
        stats.put("complaints", complaintStats);

        // Fee statistics
        Map<String, Object> feeStats = new HashMap<>();
        feeStats.put("pending", feeRepository.countByStatus("PENDING"));
        feeStats.put("overdue", feeRepository.countByStatus("OVERDUE"));

        // Calculate total revenue (paid fees)
        List<Fee> paidFees = feeRepository.findPaidFeesByYear(LocalDate.now().getYear());
        double totalRevenue = paidFees.stream().mapToDouble(Fee::getFinalAmount).sum();
        feeStats.put("totalRevenue", totalRevenue);

        // Calculate pending revenue
        List<Fee> pendingFees = feeRepository.findPendingFeesByYear(LocalDate.now().getYear());
        double pendingRevenue = pendingFees.stream().mapToDouble(Fee::getBalanceAmount).sum();
        feeStats.put("pendingRevenue", pendingRevenue);
        stats.put("fees", feeStats);

        // Leave statistics
        Map<String, Object> leaveStats = new HashMap<>();
        leaveStats.put("pending", leaveRepository.countByStatus("PENDING"));
        stats.put("leaves", leaveStats);

        return stats;
    }

    public Map<String, Object> getStudentDashboardStats(String studentId) {
        Map<String, Object> data = new HashMap<>();

        // Get student details
        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            User student = studentOpt.get();
            data.put("student", student);

            if (student.getRoomId() != null && !student.getRoomId().isEmpty()) {
                roomRepository.findById(student.getRoomId())
                        .ifPresent(r -> student.setRoomNumber(r.getRoomNumber()));
            }

            // Student statistics
            Map<String, Object> stats = new HashMap<>();

            // Complaint stats
            Map<String, Object> complaintStats = new HashMap<>();
            complaintStats.put("pending", complaintRepository.countByReportedBy(studentId));
            stats.put("complaints", complaintStats);

            // Fee stats
            Map<String, Object> feeStats = new HashMap<>();
            // Calculate pending fee amount for this student
            List<Fee> studentPendingFees = feeRepository.findStudentPendingFeesAmounts(studentId);
            double pendingAmount = studentPendingFees.stream().mapToDouble(Fee::getBalanceAmount).sum();
            feeStats.put("pendingAmount", pendingAmount);
            stats.put("fees", feeStats);

            // Leave stats
            Map<String, Object> leaveStats = new HashMap<>();
            leaveStats.put("active", leaveRepository.countByStudentIdAndStatus(studentId, "APPROVED"));
            stats.put("leaves", leaveStats);

            data.put("stats", stats);

            // Recent announcements
            data.put("recentAnnouncements", announcementRepository.findActiveAnnouncements(LocalDateTime.now()));
        }

        return data;
    }

    public Map<String, Object> getWardenDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Similar to admin but focused on warden responsibilities
        stats.put("students", userRepository.countByRoleAndIsActive("STUDENT", true));

        Map<String, Object> roomStats = new HashMap<>();
        long totalRooms = roomRepository.countByIsActive(true);
        long occupiedRooms = roomRepository.countByStatusAndIsActive("OCCUPIED", true);
        roomStats.put("total", totalRooms);
        roomStats.put("occupied", occupiedRooms);
        roomStats.put("available", totalRooms - occupiedRooms);
        stats.put("rooms", roomStats);

        Map<String, Object> complaintStats = new HashMap<>();
        complaintStats.put("pending", complaintRepository.countByStatus("PENDING"));

        // Calculate urgent complaints
        List<Complaint> urgentComplaints = complaintRepository.findUrgentComplaints();
        complaintStats.put("urgent", urgentComplaints.size());
        stats.put("complaints", complaintStats);

        Map<String, Object> leaveStats = new HashMap<>();
        leaveStats.put("pending", leaveRepository.countByStatus("PENDING"));

        // Calculate overdue leaves
        List<Leave> overdueLeaves = leaveRepository.findOverdueLeaves(LocalDate.now());
        leaveStats.put("overdue", overdueLeaves.size());
        stats.put("leaves", leaveStats);

        return Map.of("stats", stats);
    }
}