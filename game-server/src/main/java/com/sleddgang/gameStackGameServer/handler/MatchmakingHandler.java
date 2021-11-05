package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.CreateGameEvent;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.ErrorEvent;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class MatchmakingHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();


    private final BlockingQueue<Match> messageQueue;

    public MatchmakingHandler(ApplicationContext appContext) {
        this.messageQueue = appContext.getBean(BlockingQueue.class);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        GameServerMessage message = this.objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        if (message.event instanceof CreateGameEvent) {
            CreateGameEvent event = (CreateGameEvent) message.event;
            messageQueue.add(new Match(event.uuid, event.clients));
        }
        else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.UNKNOWN_EVENT), message.reqid))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketSessions.remove(session);
    }
}
