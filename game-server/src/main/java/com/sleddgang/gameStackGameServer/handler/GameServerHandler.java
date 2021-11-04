package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.ErrorEvent;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import com.sleddgang.gameStackGameServer.schemas.PingEvent;
import com.sleddgang.gameStackGameServer.schemas.PongMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class GameServerHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        GameServerMessage message = this.objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        if (message.event instanceof PingEvent){
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new PongMessage(message.reqid))));
        }
        else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(1, "Unknown event"), message.reqid))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketSessions.remove(session);
    }
}
