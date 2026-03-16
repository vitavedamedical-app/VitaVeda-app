package com.vitaveda.controller;

import com.vitaveda.entity.User;
import com.vitaveda.model.Response;
import com.vitaveda.repository.UserRepository;
import com.vitaveda.interceptor.JwtUtils;
import com.vitaveda.service.OtpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signup")
    public String register(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "Username '" + user.getUsername() + "' is already taken!";
        }
        String password = user.getPassword();
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

        if (password == null || !password.matches(passwordRegex)) {
            return "Password must be at least 8 characters long, include 1 uppercase letter, " +
                    "1 lowercase letter, 1 number, and 1 special character (@#$%^&+=!).";
        }
        if (user.getNumber() == null) {
            throw new RuntimeException("Mobile number is required.");
        }

        String numberStr = user.getNumber().toString();
        if (numberStr.length() != 10) {
            throw new RuntimeException("Mobile number must be exactly 10 digits.");
        }
        if (user.getEmailId() == null || !user.getEmailId().contains("@")) {
            throw new RuntimeException("Invalid email format.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully!";
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("invalid user_name"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtUtils.generateToken(user.getUsername());
        } else {
            return "Invalid Credentials";
        }
    }

    @PutMapping("/forget-password")
    public String forgetPassword(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + user.getUsername() + "' is already taken!");
        }
            String newPassword = user.getPassword();
            String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
            if (newPassword == null || !newPassword.matches(passwordRegex)) {
                throw new RuntimeException("New password must meet complexity requirements (8+ chars, uppercase, lowercase, number, special char).");
            }
            String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusSeconds(120));
            user.setUpdatedAt(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(newPassword));

            userRepository.save(user);

            return "Password reset successfully for " + user.getUsername();
    }

    @PostMapping("/send-otp")
    public Response sendOtp(@RequestBody Map<String, String> payload) {
        String rawIdentifier = payload.get("username");
        if (rawIdentifier == null) rawIdentifier = payload.get("email");
        if (rawIdentifier == null) rawIdentifier = payload.get("number");

        if (rawIdentifier == null) {
            return new Response("Identifier (number, email, or username) is required.", 400);
        }
        final String identifier = rawIdentifier;
        String otp = otpService.generateOtp();
        User user;
        if (identifier.contains("@") || identifier.matches("\\d{10}")) {
            if (identifier.contains("@") && userRepository.findByEmailId(identifier).isPresent()) {
                return new Response("Email already exists.", 400);
            }
            if (identifier.matches("\\d{10}") && userRepository.findByNumber(identifier).isPresent()) {
                return new Response("Number already exists.", 400);
            }
            otpService.saveTempOtp(identifier, otp);

            if (identifier.contains("@")) {
                otpService.sendEmailOtp(identifier, otp);
            } else {
                otpService.sendMobileOtp(identifier, otp);
            }
        }
        else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new RuntimeException("User with username :"+identifier+"not found."));
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusSeconds(120));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            if (user.getEmailId() != null) otpService.sendEmailOtp(user.getEmailId(), otp);
            if (user.getNumber() != null) otpService.sendMobileOtp(user.getNumber(), otp);
        }
        return new Response("OTP sent successfully.", 200);
    }
    @PostMapping("/verify-otp")
    public Response verifyOtp(@RequestBody Map<String, String> payload) {
        String rawIdentifier = payload.get("username");
        if (rawIdentifier == null) rawIdentifier = payload.get("email");
        if (rawIdentifier == null) rawIdentifier = payload.get("number");
        final String identifier = rawIdentifier;
        String otp = payload.get("otp");
        if (otpService.verifyTempOtp(identifier, otp)) {
            return new Response("Verification successful!", 200);
        }else{
            if(identifier.matches("\\d{10}") || identifier.contains("@")) return new Response("invalid OTP !", 200);
        }
        User user = userRepository.findByUsername(identifier)
                .orElseThrow(() -> new RuntimeException("invalid username: " + identifier));

        if (user.getOtp() != null && user.getOtp().equals(otp) &&
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {

            user.setMobileVerified(true);
            user.setEmailVerified(true);
            user.setOtp(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return new Response("Verification successful!", 200);
        } else {
            throw new RuntimeException("Invalid or expired OTP.");
        }
    }
}
