package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
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
    private final List<Session> webSocketSessions = new ArrayList<>();  //List of connected websockets.
                                                                        //Whenever a new connection is made it is added to this list and whenever a connection is closed it is removed.
    private final ObjectMapper objectMapper = new ObjectMapper();       //Used to serialize and deserialize json.
    private final Map<String, Match> matches = new HashMap<>();         //List of currently running matches.
    private final Object matchesMutex = new Object();                   //Used to lock matches.

    private final BlockingQueue<MatchMessage> gameMessageQueue;         //Used to receive messages from the MatchmakingHandler.
    private final BlockingQueue<Message> matchMessageQueue;             //Used to send messages to the MatchmakingHandler.

    private final int slots;        //Max number of running matches.
    private final Environment env;  //Used to get slots.

    public GameServerHandler(ConfigurableApplicationContext appContext) {
        //TODO Handle null env variables.
        //Get the slots from the passed environmental variable. You did pass and environmental variable didn't you?
        env = appContext.getBean(Environment.class);
        slots = Integer.parseInt(env.getProperty("SLOTS"));

        //Get the gameMessageQueue from the application context.
        Object gameQueue = appContext.getBean("gameMessageQueue");
        if (gameQueue instanceof BlockingQueue) {
            gameMessageQueue = (BlockingQueue<MatchMessage>) gameQueue;
        }
        else {
            gameMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
            appContext.close();
        }

        //Get the matchmakingMessageQueue from the application context.
        Object matchQueue = appContext.getBean("matchmakingMessageQueue");
        if (matchQueue instanceof BlockingQueue) {
            matchMessageQueue = (BlockingQueue<Message>) matchQueue;
        }
        else {
            matchMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
            appContext.close();
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
                    if (matches.size() < slots) {
                        //Check if we already have a match with that uuid. If so respond with DUPLICATE_MATCH otherwise add the match.
                        if (matches.containsKey(match.getUuid())) {
                            matchMessageQueue.add(new ErrorMessage(HandlerError.DUPLICATE_MATCH, match.getServer(), match.getReqid()));
                        } else {
                            matches.put(match.getUuid(), new Match(match));
                            matchMessageQueue.add(new Status(slots - matches.size()));
                        }
                    } else {
                        matchMessageQueue.add(new ErrorMessage(HandlerError.MATCHES_FULL, match.getServer(), match.getReqid()));
                    }
                } else {
                    matchMessageQueue.add(new ErrorMessage(HandlerError.INVALID_MATCH, match.getServer(), match.getReqid()));
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
        //If the message is a join then find the client's match and check if they are already in the match.
        //If they aren't then add them to the match and respond with a JoinResponse.
        else if (message.event instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) message.event;
            boolean contains = false;
            synchronized (matchesMutex) {
                //Loop through each match in matches and find the one that allows the client.
                for (Map.Entry<String, Match> match : matches.entrySet()) {
                    if (match.getValue().getAllowedClients().contains(event.clientUuid)) {
                        contains = true;
                        //Check if the client is already in a match. If so respond with a CLIENT_ALREADY_IN_MATCH error.
                        // Otherwise add the client to the match and respond to inform the client.
                        if (match.getValue().containsClient(event.clientUuid)) {
                            sendMessage(session, new ErrorEvent(Error.CLIENT_ALREADY_IN_MATCH), message.reqid);
                        }
                        else {
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
        //If the message is a move event find the match that contains the client and play their move.
        else if (message.event instanceof MoveEvent) {
            MoveEvent event = (MoveEvent) message.event;
            boolean contains = false;
            synchronized (matchesMutex) {
                for (Map.Entry<String, Match> match : matches.entrySet()) {
                    //If match contains client play the clients move.
                    if (match.getValue().containsClientBySessionId(session.getId())) {
                        contains = true;
                        //Play the clients move. This also handles the game logic and sending out the results to the clients.
                        Match.Status status = match.getValue().playMove(session.getId(), event.move, message.reqid);
                        switch (status) {
                            case ERROR: //When the status is an error we need to inform the clients, close the connection, and remove the match.
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
                            case ENDED: //When the status is ended we need to close the connection and remove the match.
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
        synchronized (matchesMutex) {
            for (Map.Entry<String, Match> match : matches.entrySet()) {
                if (match.getValue().getClients().stream().anyMatch(client -> client.getSession().getId().equals(session.getId()))) {
                    match.getValue().shutdown();
                    matches.remove(match.getKey());
                }
                break;
            }
        }
        webSocketSessions.removeIf(s -> s.getSession().getId().equals(session.getId()));
    }

    //Sends a message using session.
    private void sendMessage(WebSocketSession session, GameServerEvent event, long reqid) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(event, reqid))));
    }
}
