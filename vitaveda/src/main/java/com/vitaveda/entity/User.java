package com.vitaveda.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    @Column(name = "user_id")
    private UUID uid;

    @Column(name = "user_name", unique = true, nullable = false)
    @Pattern(regexp = "^[^@]+$", message = "Username cannot contain the '@' symbol")
    private String username;

    @Column(name = "password",nullable = false,length = 1024)
    private String password;

    @Column(name = "first_name", nullable = false, length = 250)
    private String first_name;

    @Column(name = "last_name", nullable = false, length = 250)
    private String last_name;

    @Column(name = "number", unique = true, nullable = false, length = 10)
    private String number;

    @Column(name = "email", unique = true, nullable = false, length = 500)
    private String emailId;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "mobile_verified")
    private boolean mobileVerified = false;

    @JsonIgnore
    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
}
