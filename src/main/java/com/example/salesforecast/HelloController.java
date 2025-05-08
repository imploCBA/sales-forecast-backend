package com.example.salesforecast;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String redirectToFrontend() {
        return "redirect:https://ff9b61a7-81c3-4463-966f-5f5fefda124e-00-161grc9y9hswu.worf.replit.dev";
    }
}