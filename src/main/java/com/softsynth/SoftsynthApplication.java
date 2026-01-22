package com.softsynth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoftsynthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftsynthApplication.class, args);
    }
}