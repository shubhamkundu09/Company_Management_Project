package com.softsynth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendRegistrationOtpEmail(String to, String otp, String roleMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Registration Invitation - Softsynth Software Solutions");
        message.setText(roleMessage + "\n\n" +
                "To complete your registration, please use the following OTP:\n" +
                "OTP: " + otp + "\n" +
                "This OTP is valid for " + otpExpirationMinutes + " minutes.\n\n" +
                "Steps to complete registration:\n" +
                "1. Use this OTP to verify your email\n" +
                "2. You will receive your login credentials via email\n" +
                "3. Login and update your profile\n\n" +
                "Best regards,\nSoftsynth Team");

        mailSender.send(message);
    }

    public void sendRegistrationEmail(String to, String role, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Welcome to Softsynth Software Solutions");
        message.setText("Congratulations! You are now registered as a " + role + " at Softsynth Software Solutions.\n\n" +
                "Your login credentials:\n" +
                "Email: " + to + "\n" +
                "Password: " + password + "\n\n" +
                "Important: Please login and change your password immediately.\n\n" +
                "Login URL: http://localhost:8080\n\n" +
                "Best regards,\nSoftsynth Team");

        mailSender.send(message);
    }

    // Add this field for email template
    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;
}