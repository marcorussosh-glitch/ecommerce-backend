package it.commerce.backend.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @GetMapping("/api/admin/test")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @GetMapping("/api/test")
    public String userEndpoint() {
        return "Authenticated access granted";
    }
}
