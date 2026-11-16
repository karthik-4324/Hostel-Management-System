package com.hostel.service;

import com.hostel.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_MINUTES = 30;
    private static final int RESEND_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.frontend-url:https://hostel-management-system-kappa-eight.vercel.app}")
    private String frontendUrl;

    public PasswordResetService(UserService userService, MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /** Starts a reset. Never tells the caller whether the email exists. */
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        Optional<User> found = userService.getUserByEmail(email.trim());
        if (found.isEmpty() || !found.get().isActive()) {
            return;
        }
        User user = found.get();
        LocalDateTime now = LocalDateTime.now();

        // A link was already sent less than a minute ago
        if (user.getPasswordResetExpires() != null && user.getPasswordResetExpires()
                .isAfter(now.plusMinutes(TOKEN_MINUTES).minusSeconds(RESEND_SECONDS))) {
            return;
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Only the hash is stored, so a database leak cannot be used to reset passwords
        user.setPasswordResetToken(hash(rawToken));
        user.setPasswordResetExpires(now.plusMinutes(TOKEN_MINUTES));
        mongoTemplate.save(user);

        sendResetEmail(user, frontendUrl + "/reset-password?token=" + rawToken);
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Reset link is invalid or has expired");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        User user = mongoTemplate.findOne(
                Query.query(Criteria.where("passwordResetToken").is(hash(token.trim()))), User.class);
        if (user == null || user.getPasswordResetExpires() == null
                || user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset link is invalid or has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        mongoTemplate.save(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentPassword == null || !userService.validatePassword(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        mongoTemplate.save(user);
    }

    private boolean mailConfigured() {
        return mailSender != null
                && !mailUsername.isBlank() && !"your-email@gmail.com".equals(mailUsername)
                && !mailPassword.isBlank() && !"your-app-password".equals(mailPassword);
    }

    private void sendResetEmail(User user, String link) {
        if (!mailConfigured()) {
            // Handy for development: shows the link in the backend terminal instead of
            // emailing it
            log.warn("Email is not configured. Password reset link for {}: {}", user.getEmail(), link);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(user.getEmail());
            message.setSubject("Reset your HMS password");
            message.setText("Hello " + user.getFirstName() + ",\n\n"
                    + "We received a request to reset your Hostel Management System password.\n"
                    + "Open this link to choose a new password:\n\n"
                    + link + "\n\n"
                    + "The link works once and expires in " + TOKEN_MINUTES + " minutes.\n"
                    + "If you did not ask for this, you can ignore this email.\n\n"
                    + "Hostel Management System");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Could not send the password reset email: {}", e.getMessage());
        }
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }
}