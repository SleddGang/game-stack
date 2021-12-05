package com.sleddgang.gameStackGameServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the game server application.
 *
 * @see com.sleddgang.gameStackGameServer.config.GameServerConfig
 * @author Benjamin
 */
@SpringBootApplication
public class GameServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameServerApplication.class, args);
    }
}
