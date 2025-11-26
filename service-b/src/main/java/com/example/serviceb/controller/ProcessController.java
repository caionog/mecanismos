package com.example.serviceb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ProcessController {

    @GetMapping("process")
    public String process(@RequestParam(value = "mode", defaultValue = "ok") String mode) throws InterruptedException {
        if ("error".equalsIgnoreCase(mode)) {
            throw new RuntimeException("forced error");
        }
        if ("slow".equalsIgnoreCase(mode)) {
            Thread.sleep(5000);
            return "slow response from B";
        }
        return "ok from B";
    }
}
