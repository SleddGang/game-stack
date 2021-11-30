package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.ErrorMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Message;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Status;
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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchmakingHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> webSocketSessions = new ArrayList<>(); //Contains a list of all the connected websockets connections;
    private final ObjectMapper objectMapper = new ObjectMapper();               //Used to serialize and deserialize json.

    private final String serverId;  //Id of this server
    private final Environment env;  //Used to get environmental variables.

    private final BlockingQueue<MatchMessage> gameMessageQueue; //Used to send messages to the GameServerHandler
    private final BlockingQueue<Message> matchMessageQueue;     //Used to receive messages from the GameServerHandler

    public MatchmakingHandler(ConfigurableApplicationContext appContext) {
        //Get the server id from the environmental variable.
        env = appContext.getBean(Environment.class);
        serverId = env.getProperty("ID");

        //Get the gameMessageQueue from the app context.
        Object gameQueue = appContext.getBean("gameMessageQueue");
        if (gameQueue instanceof BlockingQueue) {
            gameMessageQueue = (BlockingQueue<MatchMessage>) gameQueue;
        }
        else {
            gameMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast gameMessageQueue to BlockingQueue.");
            appContext.close();
        }

        //Get the matchMessageQueue from the app context
        Object matchQueue = appContext.getBean("matchmakingMessageQueue");
        if (matchQueue instanceof BlockingQueue) {
            matchMessageQueue = (BlockingQueue<Message>) matchQueue;
        }
        else {
            matchMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast matchMessageQueue to BlockingQueue.");
            appContext.close();
        }

        //This thread will wait for a message from the matchMessageQueue and send a status message to every connection.
        Thread thread = new Thread(() -> {
            while (true) {
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
                else if (message instanceof ErrorMessage) {
                    ErrorMessage error = (ErrorMessage) message;
                    switch (error.getError()) {
                        case MATCHES_FULL:
                            sendError(error, Error.MATCHES_FULL);
                            break;
                        case DUPLICATE_MATCH:
                            sendError(error, Error.DUPLICATE_MATCH);
                            break;
                        case INVALID_MATCH:
                            sendError(error, Error.INVALID_MATCH_ERROR);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        synchronized (webSocketSessions) {
            webSocketSessions.add(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        //Deserialize the message from textMessage
        GameServerMessage message = objectMapper.readValue(textMessage.asBytes(), GameServerMessage.class);
        //If the message is a CreateGameEven then add then new match to gameMessageQueue.
        if (message.event instanceof CreateGameEvent) {
            CreateGameEvent event = (CreateGameEvent) message.event;
            gameMessageQueue.add(new MatchMessage(event.uuid, event.clients, session.getId(), message.reqid));
        }
        //If we don't recognize the event type then respond with UNKNOWN_EVENT.
        else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(Error.UNKNOWN_EVENT), message.reqid))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized (webSocketSessions) {
            webSocketSessions.remove(session);
        }
    }

    private void sendError(ErrorMessage errorMessage, Error error) {
        synchronized (webSocketSessions) {
            if (webSocketSessions.stream().anyMatch(s -> s.getId().equals(errorMessage.getServer()))) {
                WebSocketSession session = webSocketSessions.stream().filter(s -> s.getId().equals(errorMessage.getServer())).findFirst().get();
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new ErrorEvent(error), errorMessage.getReqid()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
