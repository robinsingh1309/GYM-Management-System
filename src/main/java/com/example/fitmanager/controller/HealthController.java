package com.example.fitmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {
    
    @GetMapping("/api/v1/health")
    public String health() {
        return "GYM Management API is running";
    }
    
}
