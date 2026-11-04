package com.hostel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private FeeService feeService;

    @Autowired
    private LeaveService leaveService;

    public Map<String, Object> generateReport(String type, String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        switch (type.toLowerCase()) {
            case "students":
                report = generateStudentsReport(format, startDate, endDate);
                break;
            case "fees":
                report = generateFeesReport(format, startDate, endDate);
                break;
            case "complaints":
                report = generateComplaintsReport(format, startDate, endDate);
                break;
            case "rooms":
                report = generateRoomsReport(format, startDate, endDate);
                break;
            case "leaves":
                report = generateLeavesReport(format, startDate, endDate);
                break;
            default:
                throw new RuntimeException("Invalid report type: " + type);
        }
        
        report.put("generatedAt", LocalDateTime.now());
        report.put("format", format);
        report.put("type", type);
        
        return report;
    }

    private Map<String, Object> generateStudentsReport(String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Get student statistics
        long totalStudents = userService.countActiveUsersByRole("STUDENT");
        
        report.put("title", "Students Report");
        report.put("totalStudents", totalStudents);
        report.put("activeStudents", totalStudents);
        
        // Add more detailed statistics as needed
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalStudents);
        stats.put("active", totalStudents);
        report.put("statistics", stats);
        
        // Generate download URL (placeholder)
        report.put("url", "/api/reports/download/students." + format);
        
        return report;
    }

    private Map<String, Object> generateFeesReport(String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Integer year = endDate != null ? LocalDate.parse(endDate).getYear() : LocalDate.now().getYear();
        Map<String, Object> feeStats = feeService.getFeeStats(year);
        
        report.put("title", "Fees Report");
        report.put("year", year);
        report.put("statistics", feeStats);
        
        // Generate download URL (placeholder)
        report.put("url", "/api/reports/download/fees." + format);
        
        return report;
    }

    private Map<String, Object> generateComplaintsReport(String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Map<String, Object> complaintStats = complaintService.getComplaintStats();
        
        report.put("title", "Complaints Report");
        report.put("statistics", complaintStats);
        
        // Generate download URL (placeholder)
        report.put("url", "/api/reports/download/complaints." + format);
        
        return report;
    }

    private Map<String, Object> generateRoomsReport(String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Map<String, Object> roomStats = roomService.getRoomStats();
        
        report.put("title", "Rooms Report");
        report.put("statistics", roomStats);
        
        // Generate download URL (placeholder)
        report.put("url", "/api/reports/download/rooms." + format);
        
        return report;
    }

    private Map<String, Object> generateLeavesReport(String format, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Integer year = endDate != null ? LocalDate.parse(endDate).getYear() : LocalDate.now().getYear();
        Map<String, Object> leaveStats = leaveService.getLeaveStats(year);
        
        report.put("title", "Leaves Report");
        report.put("year", year);
        report.put("statistics", leaveStats);
        
        // Generate download URL (placeholder)
        report.put("url", "/api/reports/download/leaves." + format);
        
        return report;
    }
}