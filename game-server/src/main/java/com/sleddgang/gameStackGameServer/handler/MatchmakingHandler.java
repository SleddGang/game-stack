package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchmakingHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String serverId;
    private final Environment env;

    private final BlockingQueue<Match> gameMessageQueue;
    private final BlockingQueue<Integer> matchMessageQueue;

    public MatchmakingHandler(ApplicationContext appContext) {
        this.env = appContext.getBean(Environment.class);
        this.serverId = env.getProperty("ID");

        Object gameQueue = appContext.getBean("gameMessageQueue");

        if (gameQueue instanceof BlockingQueue) {
            this.gameMessageQueue = (BlockingQueue<Match>) gameQueue;
        }
        else {
            this.gameMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
        }

        Object matchQueue = appContext.getBean("matchmakingMessageQueue");

        if (matchQueue instanceof BlockingQueue) {
            this.matchMessageQueue = (BlockingQueue<Integer>) matchQueue;
        }
        else {
            this.matchMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
        }

        Thread thread = new Thread(() -> {
            int slots = -1;
            try {
                slots = this.matchMessageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (WebSocketSession session : webSocketSessions) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ServerStatusEvent(serverId, slots), 0))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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
            this.gameMessageQueue.add(new Match(event.uuid, event.clients));
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
