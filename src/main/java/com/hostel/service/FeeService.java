package com.hostel.service;

import com.hostel.model.Fee;
import com.hostel.repository.FeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeeService {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getAllFees(String userId, String userRole, int page, int limit, String search,
            String status, String feeType) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("dueDate").descending());

        Query query = new Query();

        // Filter based on user role
        if ("STUDENT".equals(userRole)) {
            query.addCriteria(Criteria.where("studentId").is(userId));
        }

        // Add search criteria (for admin/warden)
        if (search != null && !search.trim().isEmpty() && !"STUDENT".equals(userRole)) {
            // This would require joining with user collection to search by student name
            // For now, we'll search by fee type and description
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("feeType").regex(search, "i"),
                    Criteria.where("description").regex(search, "i"));
            query.addCriteria(searchCriteria);
        }

        // Add status filter
        if (status != null && !status.trim().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status.toUpperCase()));
        }

        // Add fee type filter
        if (feeType != null && !feeType.trim().isEmpty()) {
            query.addCriteria(Criteria.where("feeType").is(feeType));
        }

        query.with(pageable);

        List<Fee> fees = mongoTemplate.find(query, Fee.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Fee.class);

        Map<String, Object> result = new HashMap<>();
        result.put("fees", fees);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        pagination.put("total", total);
        result.put("pagination", pagination);

        return result;
    }

    public Fee getFeeById(String id) {
        Optional<Fee> optionalFee = feeRepository.findById(id);
        return optionalFee.orElse(null);
    }

    public Fee createFee(Fee fee) {
        fee.calculateFinalAmount();
        return feeRepository.save(fee);
    }

    public Fee updateFee(String id, Fee feeDetails) {
        Optional<Fee> optionalFee = feeRepository.findById(id);
        if (optionalFee.isPresent()) {
            Fee fee = optionalFee.get();
            fee.setAmount(feeDetails.getAmount());
            fee.setDueDate(feeDetails.getDueDate());
            fee.setLateFee(feeDetails.getLateFee());
            fee.setDiscount(feeDetails.getDiscount());
            fee.setDescription(feeDetails.getDescription());
            fee.setUpdatedBy(feeDetails.getUpdatedBy());
            fee.calculateFinalAmount();
            return feeRepository.save(fee);
        }
        return null;
    }

    public Fee addPayment(String id, Fee.PaymentHistory payment) {
        Optional<Fee> optionalFee = feeRepository.findById(id);
        if (optionalFee.isPresent()) {
            Fee fee = optionalFee.get();
            if (payment.getAmount() == null || payment.getAmount() <= 0) {
                throw new RuntimeException("Payment amount must be greater than zero");
            }
            if (payment.getAmount() > fee.getBalanceAmount()) {
                throw new RuntimeException("Payment is more than the balance due");
            }
            fee.getPaymentHistory().add(payment);
            fee.setPaidAmount(fee.getPaidAmount() + payment.getAmount());
            fee.setPaymentMethod(payment.getPaymentMethod());
            fee.setTransactionId(payment.getTransactionId());
            fee.calculateFinalAmount();
            return feeRepository.save(fee);
        }
        return null;
    }

    public void deleteFee(String id) {
        feeRepository.deleteById(id);
    }

    public List<Fee> getFeesByStudentId(String studentId) {
        return feeRepository.findByStudentId(studentId);
    }

    public List<Fee> getFeesByStatus(String status) {
        return feeRepository.findByStatus(status);
    }

    public Map<String, Object> getDefaulters() {
        List<Fee> defaulters = feeRepository.findDefaulters();

        Map<String, Object> result = new HashMap<>();
        result.put("defaulters", defaulters);
        result.put("total", defaulters.size());

        return result;
    }

    public List<Fee> getOverdueFees() {
        return feeRepository.findOverdueFees(LocalDate.now());
    }

    public Map<String, Object> getFeeStats(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        Map<String, Object> stats = new HashMap<>();

        stats.put("total", feeRepository.countByYear(year));
        stats.put("paid", feeRepository.findPaidFeesByYear(year).size());
        stats.put("pending", feeRepository.findPendingFeesByYear(year).size());
        stats.put("overdue", feeRepository.countByStatus("OVERDUE"));

        // Calculate revenue
        List<Fee> paidFees = feeRepository.findPaidFeesByYear(year);
        double totalRevenue = paidFees.stream().mapToDouble(Fee::getFinalAmount).sum();

        List<Fee> pendingFees = feeRepository.findPendingFeesByYear(year);
        double pendingRevenue = pendingFees.stream().mapToDouble(Fee::getBalanceAmount).sum();

        stats.put("totalRevenue", totalRevenue);
        stats.put("pendingRevenue", pendingRevenue);
        stats.put("year", year);

        return stats;
    }
}