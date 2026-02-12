package com.pickleball;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    void checkHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "admin1234";
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        boolean matches = encoder.matches(raw, hash);

        if (!matches) {
            String newHash = encoder.encode(raw);
            try (java.io.FileWriter fw = new java.io.FileWriter("hash.txt")) {
                fw.write(newHash);
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new RuntimeException("HASH MISMATCH! New hash written to hash.txt");
        }
        System.out.println("HASH MATCHES!");
    }
}
