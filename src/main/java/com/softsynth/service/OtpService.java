package com.softsynth.service;

import com.softsynth.entity.Otp;
import com.softsynth.entity.User;
import com.softsynth.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    private static final String OTP_CHARACTERS = "0123456789";
    private static final int OTP_LENGTH = 6;

    public String generateOtp(String email, User.Role role, Long managerId) {
        // Delete expired OTPs
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        // Generate new OTP
        Random random = new Random();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARACTERS.charAt(random.nextInt(OTP_CHARACTERS.length())));
        }

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp.toString());
        otpEntity.setRole(role);
        otpEntity.setManagerId(managerId);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpEntity.setUsed(false);

        otpRepository.save(otpEntity);

        // Send OTP via email with role-specific message
        String roleMessage = "";
        switch (role) {
            case MANAGER:
                roleMessage = "You are being registered as a Manager at Softsynth Software Solutions.";
                break;
            case EMPLOYEE:
                roleMessage = "You are being registered as an Employee at Softsynth Software Solutions.";
                break;
            default:
                roleMessage = "You are being registered at Softsynth Software Solutions.";
        }

        emailService.sendRegistrationOtpEmail(email, otp.toString(), roleMessage);

        return otp.toString();
    }

    public Optional<Otp> validateOtp(String email, String otp) {
        return otpRepository.findByEmailAndOtpAndIsUsedFalse(email, otp)
                .filter(o -> o.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    public void markOtpAsUsed(Otp otp) {
        otp.setUsed(true);
        otpRepository.save(otp);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}