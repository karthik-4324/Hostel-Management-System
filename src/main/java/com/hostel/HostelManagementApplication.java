package com.hostel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@ComponentScan("com.hostel")
@EnableMongoAuditing
public class HostelManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(HostelManagementApplication.class, args);
    }
}