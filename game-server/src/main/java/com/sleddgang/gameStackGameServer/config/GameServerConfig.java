package com.sleddgang.gameStackGameServer.config;

import com.sleddgang.gameStackGameServer.handler.GameServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class GameServerConfig implements WebSocketConfigurer {

    private static final String CHAT_ENDPOINT = "/game";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getGameServerWebSocketHandler(), CHAT_ENDPOINT).setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler getGameServerWebSocketHandler() {
        return new GameServerHandler();
    }
}
