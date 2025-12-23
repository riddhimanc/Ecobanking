package com.ecobank.core.controllers;


// src/main/java/com/ecobank/core/web/LoginController.java


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }
}
