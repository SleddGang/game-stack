package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.shcemas.Match;
import com.sleddgang.gameStackGameServer.handler.shcemas.Message;
import com.sleddgang.gameStackGameServer.handler.shcemas.Status;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
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
    private final BlockingQueue<Message> matchMessageQueue;

    public MatchmakingHandler(ApplicationContext appContext) {
        env = appContext.getBean(Environment.class);
        serverId = env.getProperty("ID");

        Object gameQueue = appContext.getBean("gameMessageQueue");

        if (gameQueue instanceof BlockingQueue) {
            gameMessageQueue = (BlockingQueue<Match>) gameQueue;
        }
        else {
            gameMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
        }

        Object matchQueue = appContext.getBean("matchmakingMessageQueue");

        if (matchQueue instanceof BlockingQueue) {
            matchMessageQueue = (BlockingQueue<Message>) matchQueue;
        }
        else {
            matchMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
            //TODO exit because this is unrecoverable.
        }

        Thread thread = new Thread(() -> {
//            int slots = -1;
            Message message = null;
            try {
                message = matchMessageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (message instanceof Status) {
                for (WebSocketSession session : webSocketSessions) {
                    try {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ServerStatusEvent(serverId, ((Status) message).getSlots()), 0))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        GameServerMessage message = objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        if (message.event instanceof CreateGameEvent) {
            CreateGameEvent event = (CreateGameEvent) message.event;
            gameMessageQueue.add(new Match(event.uuid, event.clients));
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
