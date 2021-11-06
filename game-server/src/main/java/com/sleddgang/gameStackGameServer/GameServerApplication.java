package com.sleddgang.gameStackGameServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
public class GameServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameServerApplication.class, args);
    }
}
