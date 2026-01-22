package com.softsynth.config;

import com.softsynth.entity.Admin;
import com.softsynth.entity.User;
import com.softsynth.repository.AdminRepository;
import com.softsynth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // we are adding deafualt admin here
        if (userRepository.findByEmail("admin@softsynth.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail("admin@softsynth.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFirstName("Shubham");
            admin.setLastName("Kundu");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            admin.setVerified(true);

            adminRepository.save(admin);

//            this is for printing on console
            System.out.println("Default admin created: admin@softsynth.com / admin");
        }
    }
}