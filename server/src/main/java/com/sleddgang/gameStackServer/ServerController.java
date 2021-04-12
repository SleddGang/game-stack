package com.sleddgang.gameStackServer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {
    @GetMapping("/test")
    String all() {
        return "It worked";
    }
}
