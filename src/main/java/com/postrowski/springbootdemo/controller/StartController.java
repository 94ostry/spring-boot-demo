package com.postrowski.springbootdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class StartController {

    @GetMapping(path = "start", produces = "application/json")
    public LocalDateTime start() {
        return LocalDateTime.now();
    }

}
