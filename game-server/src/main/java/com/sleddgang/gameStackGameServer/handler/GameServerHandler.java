package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.*;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameServerHandler extends TextWebSocketHandler {
    private final List<Session> webSocketSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Match> matches = new HashMap<>();
    private final Object matchesMutex = new Object();

    private final BlockingQueue<MatchMessage> gameMessageQueue;
    private final BlockingQueue<Message> matchMessageQueue;

    private final int slots;
    private final Environment env;

    @Autowired
    private TaskExecutor threadPoolTaskExecutor;

    public GameServerHandler(ApplicationContext appContext) {
//        this.slots = (int) appContext.getBean("slots");
        //TODO Handle null env variables.
        env = appContext.getBean(Environment.class);
        slots = Integer.parseInt(env.getProperty("SLOTS"));

        Object gameQueue = appContext.getBean("gameMessageQueue");

        if (gameQueue instanceof BlockingQueue) {
            gameMessageQueue = (BlockingQueue<MatchMessage>) gameQueue;
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
        }

        //This tread will listen for matches from the matchmaker handler and respond appropriately.
        Thread thread = new Thread(() -> {
            while (true) {
                MatchMessage match = null;
                try {
                    match = gameMessageQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Check if we have a match. If not then respond with INVALID_MATCH.
                if (match != null) {
                    //Check if we have any available slots. If not respond with MATCHES_FULL
                    if (matches.size() <= slots) {
                        //Check if we already have a match with that uuid. If so respond with DUPLICATE_MATCH otherwise add the match.
                        if (matches.containsKey(match.getUuid())) {
                            matchMessageQueue.add(new ErrorMessage(HandlerError.DUPLICATE_MATCH));
                        } else {
                            matches.put(match.getUuid(), new Match(match));
                            matchMessageQueue.add(new Status(slots - matches.size()));
                        }
                    } else {
                        matchMessageQueue.add(new ErrorMessage(HandlerError.MATCHES_FULL));
                    }
                } else {
                    matchMessageQueue.add(new ErrorMessage(HandlerError.INVALID_MATCH));
                }
            }
        });
        thread.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessions.add(new Session(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        //TODO Handle invalid json.

        //Get the client from the matches. If the client is not in a match then client will be null.
        Session client = null;
        GameServerMessage message = objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        for (Session s : webSocketSessions) {
            if (s.getSession().getId().equals(session.getId())) {
                client = s;
                break;
            }
        }

        //Check if the client has already used message's reqid. If so send an error back otherwise add the reqid to the client's list of reqids.
        if (client != null && client.getReqids().contains(message.reqid)) {
            sendMessage(session, new ErrorEvent(Error.INVALID_REQID), message.reqid);
            return;
        }
        else if (client != null) {
            client.getReqids().add(message.reqid);
        }

        //If the message is a ping respond with a pong.
        if (message.event instanceof PingEvent) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new PongMessage(message.reqid))));
        }
        //If the message is a join then find the client's match and check if they are already in the match. If they aren't then add them to the match and respond with a JoinResponse.
        else if (message.event instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) message.event;
            boolean contains = false;
            synchronized (matchesMutex) {
                for (Map.Entry<String, Match> match : matches.entrySet()) {
                    if (match.getValue().getAllowedClients().contains(event.clientUuid)) {
                        contains = true;
                        if (match.getValue().containsClient(event.clientUuid)) {
                            sendMessage(session, new ErrorEvent(Error.CLIENT_ALREADY_IN_MATCH), message.reqid);
                        } else {
                            match.getValue().addClient(new Client(event.clientUuid, session));
                            sendMessage(session, new JoinResponse(match.getValue().getUuid()), message.reqid);
                        }
                        break;
                    }
                }
            }
            //If the client is not authorized to join a match then respond with a INVALID_CLIENT error.
            if (!contains) {
                sendMessage(session, new ErrorEvent(Error.INVALID_CLIENT), message.reqid);
            }
        }
        else if (message.event instanceof MoveEvent) {
            MoveEvent event = (MoveEvent) message.event;
            boolean contains = false;
            synchronized (matchesMutex) {
                for (Map.Entry<String, Match> match : matches.entrySet()) {
                    if (match.getValue().containsClientBySessionId(session.getId())) {
                        contains = true;
                        Match.Status status = match.getValue().playMove(session.getId(), event.move, message.reqid);
                        switch (status) {
                            case ERROR:
                                match.getValue().getClients().forEach((c) -> {
                                    try {
                                        sendMessage(c.getSession(), new ErrorEvent(Error.MATCH_ERROR), message.reqid);
                                        c.getSession().close();
                                    } catch (IOException e) {
                                        //TODO Figure out when this will throw an exception.
                                        e.printStackTrace();
                                    }
                                });
                                matches.remove(match.getKey());
                                break;
                            case ENDED:
                                match.getValue().getClients().forEach((c) -> {
                                    try {
                                        c.getSession().close();
                                    } catch (IOException e) {
                                        //TODO Figure out when this will throw an exception.
                                        e.printStackTrace();
                                    }
                                });
                                matches.remove(match.getKey());
                                break;
                        }
                        break;
                    }
                }
            }
            //If the client is not authorized to join a match then respond with a INVALID_CLIENT error.
            if (!contains) {
                sendMessage(session, new ErrorEvent(Error.INVALID_CLIENT), message.reqid);
            }
        }
        //If we don't recognize the event type then respond with an UNKNOWN_EVENT error.
        else {
            sendMessage(session, new ErrorEvent(Error.UNKNOWN_EVENT), message.reqid);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketSessions.removeIf(s -> s.getSession().getId().equals(session.getId()));
    }

    private void sendMessage(WebSocketSession session, GameServerEvent event, long reqid) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(event, reqid))));
    }
}
