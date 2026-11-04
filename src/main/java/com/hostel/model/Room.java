package com.hostel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;

    @NotBlank(message = "Room number is required")
    @Indexed(unique = true)
    private String roomNumber;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;

    @NotBlank(message = "Block is required")
    private String block;

    @NotBlank(message = "Room type is required")
    private String type; // single, double, triple

    @NotNull(message = "Room capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Integer currentOccupancy = 0;

    private List<Occupant> occupants = new ArrayList<>();

    private List<String> amenities = new ArrayList<>();

    @NotNull(message = "Monthly rent is required")
    @Min(value = 0, message = "Rent cannot be negative")
    private Double monthlyRent;

    @NotNull(message = "Security deposit is required")
    @Min(value = 0, message = "Security deposit cannot be negative")
    private Double securityDeposit;

    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED

    private List<MaintenanceRecord> maintenanceHistory = new ArrayList<>();

    private List<String> images = new ArrayList<>();

    private String description;

    private boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Room() {
    }



    public Room(String roomNumber, Integer floor, String block, String type, Integer capacity, Double monthlyRent,
            Double securityDeposit) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.block = block;
        this.type = type;
        this.capacity = capacity;
        this.monthlyRent = monthlyRent;
        this.securityDeposit = securityDeposit;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public List<Occupant> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<Occupant> occupants) {
        this.occupants = occupants;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public Double getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(Double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public Double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(Double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MaintenanceRecord> getMaintenanceHistory() {
        return maintenanceHistory;
    }

    public void setMaintenanceHistory(List<MaintenanceRecord> maintenanceHistory) {
        this.maintenanceHistory = maintenanceHistory;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status) && currentOccupancy < capacity;
    }

    public double getOccupancyPercentage() {
        return capacity > 0 ? (double) currentOccupancy / capacity * 100 : 0;
    }

    // Nested classes
    public static class Occupant {
        private String studentId;
        private LocalDateTime allocatedDate;
        private Integer bedNumber;

        public Occupant() {
        }

        public Occupant(String studentId, Integer bedNumber) {
            this.studentId = studentId;
            this.bedNumber = bedNumber;
            this.allocatedDate = LocalDateTime.now();
        }

        // Getters and Setters
        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public LocalDateTime getAllocatedDate() {
            return allocatedDate;
        }

        public void setAllocatedDate(LocalDateTime allocatedDate) {
            this.allocatedDate = allocatedDate;
        }

        public Integer getBedNumber() {
            return bedNumber;
        }

        public void setBedNumber(Integer bedNumber) {
            this.bedNumber = bedNumber;
        }
    }

    public static class MaintenanceRecord {
        private String issue;
        private LocalDateTime reportedDate;
        private LocalDateTime resolvedDate;
        private Double cost;
        private String description;
        private String reportedBy;

        // Getters and Setters
        public String getIssue() {
            return issue;
        }

        public void setIssue(String issue) {
            this.issue = issue;
        }

        public LocalDateTime getReportedDate() {
            return reportedDate;
        }

        public void setReportedDate(LocalDateTime reportedDate) {
            this.reportedDate = reportedDate;
        }

        public LocalDateTime getResolvedDate() {
            return resolvedDate;
        }

        public void setResolvedDate(LocalDateTime resolvedDate) {
            this.resolvedDate = resolvedDate;
        }

        public Double getCost() {
            return cost;
        }

        public void setCost(Double cost) {
            this.cost = cost;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReportedBy() {
            return reportedBy;
        }

        public void setReportedBy(String reportedBy) {
            this.reportedBy = reportedBy;
        }
    }
}