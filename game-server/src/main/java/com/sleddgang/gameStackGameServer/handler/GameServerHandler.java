package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class GameServerHandler extends TextWebSocketHandler {
    private final List<Session> webSocketSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Match> matches = new ArrayList<>();
    private final Object matchesMutex = new Object();

    private final BlockingQueue<Match> messageQueue;

    public GameServerHandler(ApplicationContext appContext) {
        this.messageQueue = appContext.getBean(BlockingQueue.class);

        Thread thread = new Thread(() -> {
            Match match = null;
            try {
                match = this.messageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (match != null) {
                boolean contains = false;
                synchronized (this.matchesMutex) {
                    for (Match m : this.matches) {
                        if (m.getUuid().equals(match.getUuid())) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        this.matches.add(match);
                    }
                    else {
                        //TODO Implement sending error back to matchmaking server.
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.webSocketSessions.add(new Session(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        //TODO Handle invalid json.

        //Get the client from the matches. If the client is not in a match then client will be null.
        Session client = null;
        GameServerMessage message = this.objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        for (Session s : this.webSocketSessions) {
            if (s.getSession().getId().equals(session.getId())) {
                client = s;
                break;
            }
        }

        //
        if (client != null && client.getReqids().contains(message.reqid)) {
            session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.INALID_REQID), message.reqid))));
            return;
        }
        else if (client != null) {
            client.getReqids().add(message.reqid);
        }

        if (message.event instanceof PingEvent) {
            session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new PongMessage(message.reqid))));
        }
        else if (message.event instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) message.event;
            boolean contains = false;
            synchronized (this.matchesMutex) {
                for (Match match : this.matches) {
                    if (match.getAllowedClients().contains(event.clientUuid)) {
                        contains = true;
                        if (match.containsClient(event.clientUuid)) {
                            session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.CLIENT_ALREADY_IN_MATCH), message.reqid))));
                        } else {
                            match.addClient(new Client(event.clientUuid, session.getId()));
                            session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new JoinResponse(match.getUuid()), message.reqid))));
                        }
                        break;
                    }
                }
            }
            if (!contains) {
                session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.INVALID_CLIENT), message.reqid))));
            }
        }
        else {
            session.sendMessage(new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.UNKNOWN_EVENT), message.reqid))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.webSocketSessions.removeIf(s -> s.getSession().getId().equals(session.getId()));
    }
}
