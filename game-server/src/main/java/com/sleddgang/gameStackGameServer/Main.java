package com.sleddgang.gameStackGameServer;

import com.sleddgang.gameStackGameServer.config.GameServerConfig;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
                GameServerConfig.class);

        context.close();
    }
}
