package com.sleddgang.gameStackGameServer.config;

import com.sleddgang.gameStackGameServer.handler.GameServerHandler;
import com.sleddgang.gameStackGameServer.handler.MatchmakingHandler;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.ClientMessage;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.AbstractHandlerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GameServerConfig configures the WebSocket handlers. It also passes the autowired application context
 * to each handler. This allows the handlers to access different beans.
 *
 * @see com.sleddgang.gameStackGameServer.GameServerApplication
 * @author Benjamin
 */
@Configuration
@EnableWebSocket
@ComponentScan("com.sleddgang.gameStackGameServer")
public class GameServerConfig implements WebSocketConfigurer {

    private static final String GAME_ENDPOINT = "/game";
    private static final String MATCHMAKING_ENDPOINT = "/matchmaking";

    //TODO This should not be necessary. The handler should be able to autowire any bean themselves. Fix it.
    @Autowired
    private ConfigurableApplicationContext appContext;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
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

    /**
     * Used to pass messages to the matchmaking handler.
     *
     * @return A message queue to pass messages to the matchmaking handler.
     */
    //Used to pass messages to the matchmaking handler.
    @Bean(name = "matchmakingMessageQueue")
    public BlockingQueue<AbstractHandlerMessage> getMatchmakingMessageQueue() {
        return new LinkedBlockingQueue<>();
    }

    /**
     * Used to pass messages to the game handler.
     *
     * @return A message queue to pass messages to the game server handler.
     */
    //Used to pass messages to the game server handler.
    @Bean(name = "gameMessageQueue")
    public BlockingQueue<ClientMessage> getGameMessageQueue() {
        return new LinkedBlockingQueue<>();
    }
}
