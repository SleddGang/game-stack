package com.sleddgang.gameStackGameServer.config;

import com.sleddgang.gameStackGameServer.handler.GameServerHandler;
import com.sleddgang.gameStackGameServer.handler.Match;
import com.sleddgang.gameStackGameServer.handler.MatchmakingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@EnableWebSocket
public class GameServerConfig implements WebSocketConfigurer {

    private static final String GAME_ENDPOINT = "/game";
    private static final String MATCHMAKING_ENDPOINT = "/matchmaking";

    @Autowired
    private ApplicationContext appContext;

//    private final BlockingQueue<MatchmakingMessage> queue = new LinkedBlockingQueue<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        while (true) {
//            System.out.println("");
//        }
        registry.addHandler(getGameServerWebSocketHandler(), GAME_ENDPOINT).setAllowedOrigins("*");
        registry.addHandler(getMatchmakingWebSocketHandler(), MATCHMAKING_ENDPOINT).setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler getGameServerWebSocketHandler() {
        return new GameServerHandler(appContext);
    }

    @Bean
    public WebSocketHandler getMatchmakingWebSocketHandler() {
        return new MatchmakingHandler(appContext);
    }

    @Bean
    public BlockingQueue<Match> getMatchmakingMessageQueue() {
        return new LinkedBlockingQueue<>();
    }
}
