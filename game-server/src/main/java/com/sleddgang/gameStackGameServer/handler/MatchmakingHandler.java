package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.ErrorMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.AbstractHandlerMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Status;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.events.ServerStatusReply;
import com.sleddgang.gameStackGameServer.schemas.methods.CreateGameMethod;
import com.sleddgang.gameStackGameServer.schemas.replies.ErrorReply;
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

/**
 * The MatchmakingHandler handles the connection with the matchmaking server.
 * It keeps track of all the matchmaking server connections in webSocketSession.
 * Whenever matchmaking server connects it gets added to the webSocketSessions and
 * whenever the matchmaking server disconnects it gets removed.
 * The MatchmakingHandler also handles messages from the matchmaking server and
 * creates matches as requested. Whenever a match is made the handler will update
 * the matchmaking server on the number of slots left on this game server and
 * if an error occurs making the match the handler will inform the matchmaking server.
 *
 * @author Benjamin
 */
public class MatchmakingHandler extends TextWebSocketHandler {
    /**
     * WebSocketSessions contains all the currently connected sessions.
     */
    private final List<WebSocketSession> webSocketSessions = new ArrayList<>(); //Contains a list of all the connected websockets connections;

    private final ObjectMapper objectMapper = new ObjectMapper();               //Used to serialize and deserialize json.

    /**
     * Id of this server. Gets set by an environmental variable.
     */
    private final String serverId;  //Id of this server
    private final Environment env;  //Used to get environmental variables.

    /**
     * Used to send messages to the {@link GameServerHandler}.
     * <p>See {@link GameServerHandler}'s constructor for the code that listens for the these messages.</p>
     */
    private final BlockingQueue<MatchMessage> gameMessageQueue; //Used to send messages to the GameServerHandler

    /**
     * Used to send receive messages from the {@link GameServerHandler}.
     */
    private final BlockingQueue<AbstractHandlerMessage> matchMessageQueue;     //Used to receive messages from the GameServerHandler

    /**
     * This constructor sets up the serverId as well as the gameMessageQueue and the matchMessageQueue.
     * It starts up a thread that will listen for messages on the matchMessageQueue.
     * If the message on the queue is a status message then a status message will be sent to all connected matchmaking
     * servers. This might change in the future.  If it is an error the error will be sent to the appropriate
     * matchmaking server.
     *
     * @param appContext    Used get the id from the environmental variable ID and
     *                      the game and match message queues from spring.
     */
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
            matchMessageQueue = (BlockingQueue<AbstractHandlerMessage>) matchQueue;
        }
        else {
            matchMessageQueue = new LinkedBlockingQueue<>();
            System.out.println("Unable to cast matchMessageQueue to BlockingQueue.");
            appContext.close();
        }

        //This thread will wait for a message from the matchMessageQueue and send a status message to every connection.
        Thread thread = new Thread(() -> {
            while (true) {
                AbstractHandlerMessage message = null;
                try {
                    message = matchMessageQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (message instanceof Status) {
                    for (WebSocketSession session : webSocketSessions) {
                        try {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new ServerStatusReply(serverId, ((Status) message).getSlots()))));
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

    /**
     * This function gets called whenever a new matchmaking server is connected.
     * It will add the session to webSocketSessions.
     *
     * @param session   Matchmaking server session.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        synchronized (webSocketSessions) {
            webSocketSessions.add(session);
        }
    }

    /**
     * This function gets called whenever a message is sent from a matchmaking server.
     * It will deserialize the message and if it contains a create game event then
     * it will add a create game message to the game server queue.
     * If it does not contain a create game event it will respond with an unknown event error.
     *
     * @param session       WebSocket session of the connected matchmaking server.
     * @param textMessage   Contents of the matchmaking server message.
     * @throws IOException  Gets thrown whenever there is an error deserializing the message or
     *                      sending a message back to the matchmaking server.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
        //Deserialize the message from textMessage
        AbstractGameMessage message = objectMapper.readValue(textMessage.asBytes(), AbstractGameMessage.class);
        //If the message is a CreateGameMethod then add the new match to gameMessageQueue.
        if (message instanceof CreateGameMethod) {
            CreateGameMethod method = (CreateGameMethod) message;
            gameMessageQueue.add(new MatchMessage(method.uuid, method.clients, session.getId(), method.reqid));
        }
        //If we don't recognize the event type then respond with UNKNOWN_EVENT.
        else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new ErrorReply(Error.UNKNOWN_MESSAGE, -1))));
        }
    }

    /**
     * Gets called whenever a connection to a matchmaking server is closed.
     * This function removes the closing session form webSocketSessions.
     *
     * @param session   Matchmaking server's WebSocket session
     * @param status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        synchronized (webSocketSessions) {
            webSocketSessions.remove(session);
        }
    }

    /**
     * This function will send an error message to the appropriate matchmaking server.
     *
     * @param errorMessage  Error message that contains the matchmaking server id and
     *                      the reqid that the error is responding to.
     * @param error         Type of error to send to the matchmaking server.
     */
    private void sendError(ErrorMessage errorMessage, Error error) {
        synchronized (webSocketSessions) {
            if (webSocketSessions.stream().anyMatch(s -> s.getId().equals(errorMessage.getServer()))) {
                WebSocketSession session = webSocketSessions.stream().filter(s -> s.getId().equals(errorMessage.getServer())).findFirst().get();
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new ErrorReply(error, errorMessage.getReqid()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
