package com.pickleball;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    void checkHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "admin1234";
        // Updated correct hash from V4 migration
        String hash = "$2a$10$mBHMCtnWe/As/3lFgjIWq.5ShQYYafrOoOPBwlZi7qtwhmlogrZ8i";

        boolean matches = encoder.matches(raw, hash);

        Assertions.assertTrue(matches, "Password should match the hash");
        System.out.println("HASH MATCHES!");
    }
}
