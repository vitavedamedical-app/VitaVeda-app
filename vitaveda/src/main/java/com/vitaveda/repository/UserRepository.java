package com.vitaveda.repository;

import com.vitaveda.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByNumber(String number);
    Optional<User> findByEmailId(String emailId);
}
