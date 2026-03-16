package com.vitaveda.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String twilioNumber;

    private final ConcurrentHashMap<String, OtpData> tempStore = new ConcurrentHashMap<>();

    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    public String generateOtp() {
        return String.valueOf((int)((Math.random() * 900000) + 100000));
    }

    public void saveTempOtp(String identifier, String otp) {
        tempStore.put(identifier, new OtpData(otp, LocalDateTime.now().plusSeconds(120)));
    }

    public boolean verifyTempOtp(String identifier, String otp) {
        OtpData data = tempStore.get(identifier);
        if (data != null && data.otp.equals(otp) && data.expiry.isAfter(LocalDateTime.now())) {
            tempStore.remove(identifier);
            return true;
        }
        return false;
    }

    public void sendEmailOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Vitaveda - Your Verification Code");
        message.setText("Your OTP is: " + otp + ". Valid for 30 seconds.");
        mailSender.send(message);
    }

    public void sendMobileOtp(String mobileNumber, String otp) {
                Twilio.init(accountSid, authToken);
            String formattedNumber = "+91" + mobileNumber;
            Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioNumber),
                    "Your Vitaveda verification code is: " + otp
            ).create();
    }

}
