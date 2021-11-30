package com.sleddgang.gameStackGameServer.config;

import com.sleddgang.gameStackGameServer.handler.GameServerHandler;
import com.sleddgang.gameStackGameServer.handler.Match;
import com.sleddgang.gameStackGameServer.handler.MatchmakingHandler;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@EnableWebSocket
@ComponentScan("com.sleddgang.gameStackGameServer")
public class GameServerConfig implements WebSocketConfigurer {

    private static final String GAME_ENDPOINT = "/game";
    private static final String MATCHMAKING_ENDPOINT = "/matchmaking";

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    Environment env;

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

    @Bean(name = "matchmakingMessageQueue")
    public BlockingQueue<Message> getMatchmakingMessageQueue() {
        return new LinkedBlockingQueue<>();
    }
    @Bean(name = "gameMessageQueue")
    public BlockingQueue<MatchMessage> getGameMessageQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}
