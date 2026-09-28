package com.uptimeguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UptimeGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(UptimeGuardApplication.class, args);
    }
}
