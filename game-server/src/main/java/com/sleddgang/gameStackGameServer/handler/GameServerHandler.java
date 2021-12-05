package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.replies.ErrorReply;
import com.sleddgang.gameStackGameServer.schemas.methods.JoinMethod;
import com.sleddgang.gameStackGameServer.schemas.methods.MoveMethod;
import com.sleddgang.gameStackGameServer.schemas.methods.PingMethod;
import com.sleddgang.gameStackGameServer.schemas.replies.JoinReply;
import com.sleddgang.gameStackGameServer.schemas.replies.PongReply;
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

/**
 * The GameServerHandler handles clients connecting and disconnecting as well as client messages.
 *
 * @author  Benjamin
 */
public class GameServerHandler extends TextWebSocketHandler {
    /**
     * WebSocketSessions contains all the currently connected sessions.
     */
    private final List<Session> webSocketSessions = new ArrayList<>();  //List of connected websockets.
                                                                        //Whenever a new connection is made it is added to this list and whenever a connection is closed it is removed.
    private final ObjectMapper objectMapper = new ObjectMapper();       //Used to serialize and deserialize json.

    /**
     * Contains a list of matches that are currently running on the server.
     */
    private final Map<String, Match> matches = new HashMap<>();         //List of currently running matches.
    private final Object matchesMutex = new Object();                   //Used to lock matches.

    /**
     * Used to send receive messages from the {@link MatchmakingHandler}.
     */
    private final BlockingQueue<MatchMessage> gameMessageQueue;         //Used to receive messages from the MatchmakingHandler.

    /**
     * Used to send messages to the {@link MatchmakingHandler}.
     * <p>See {@link MatchmakingHandler}'s constructor for the code that listens for the these messages.</p>
     */
    private final BlockingQueue<AbstractHandlerMessage> matchMessageQueue;             //Used to send messages to the MatchmakingHandler.

    /**
     * Maximum number of matches that's allowed on the server at once.
     * Slots gets set by the environmental variable SLOTS.
     */
    private final int slots;        //Max number of running matches.
    private final Environment env;  //Used to get slots.

    /**
     * This constructor sets up slots as well as gameMessageQueue and matchMessageQueue.
     * It also creates the thread that listens for messages on the gameMessageQueue.
     * Whenever it receives a message it creates a match based on the message information however
     * if it runs into an error it will inform the {@link MatchmakingHandler} using the matchMessageQueue.
     *
     * @param appContext Current application context used to get slots gameMessageQueue and matchMessageQueue.
     */
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
            matchMessageQueue = (BlockingQueue<AbstractHandlerMessage>) matchQueue;
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

    /**
     * This function gets called whenever a new client connects and will add the client session to webSocketSessions.
     *
     * @param session Incoming client session.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketSessions.add(new Session(session));
    }

    /**
     * This function gets called whenever a client sends a message to the server.
     * It will deserialize the message from json and call handleMethod if it is a method.
     * <p>If the message type is not recognized then send the client an unknown message error.</p>
     *
     * @param session       WebSocket session that is connected to the client sending the message.
     * @param textMessage   Contents of the clients message.
     * @throws IOException  Gets thrown whenever the client sends invalid json or a message fails to send.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
        //TODO Handle invalid json.


        AbstractGameMessage message = objectMapper.readValue(textMessage.asBytes(), AbstractGameMessage.class);

        //Check if the message is a method and if so figure out what to do with it.
        if (message instanceof AbstractGameMethod) {
            handleMethod(session, (AbstractGameMethod) message);
        }
        //If the message is not a method then respond with an UNKNOWN_MESSAGE error with an id of -1.
        else {
            sendMessage(session, new ErrorReply(Error.UNKNOWN_MESSAGE, -1));
        }
    }

    /**
     * This function gets called whenever a client disconnects from the server.
     * If the client is in a match that match gets informed that the other client has disconnected
     * and will disconnect the remaining clients.
     * The client session will also get removed from webSocketSessions.
     *
     * @param session   Clients WebSocket session.
     * @param status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
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

    /**
     * HandleMethod takes in a clients method and responds appropriately.
     * It first checks if the client has already used the message reqid.
     * If the client has then it will inform the client with an INVALID_REQID error.
     * It will then determine what type of method is in the message.
     * <p>If it is a ping method it will respond with a pong.</p>
     * <p>If it is a join method it will add the client to the appropriate match assuming the client is authorized to do so and
     * the client is not already in that match.</p>
     * <p>If it is a move method it will play the selected move.</p>
     * <p>If we don't recognize the method type then respond with an UNKNOWN_MESSAGE error.</p>
     *
     * @param session       Client WebSocket session
     * @param method        Client message.
     * @throws IOException  Gets thrown whenever a message to the client fails to send.
     */
    private void handleMethod(WebSocketSession session, AbstractGameMethod method) throws IOException {
        //Get the client from the matches. If the client is not in a match then client will be null.
        Session client = null;
        for (Session s : webSocketSessions) {
            if (s.getSession().getId().equals(session.getId())) {
                client = s;
                break;
            }
        }

        //Check if the client has already used message's reqid. If so send an error back otherwise add the reqid to the client's list of reqids.
        if (client != null && client.getReqids().contains(method.reqid)) {
            sendMessage(session, new ErrorReply(Error.INVALID_REQID, method.reqid));
            return;
        }
        else if (client != null) {
            client.getReqids().add(method.reqid);
        }

        //If the message is a ping respond with a pong.
        if (method instanceof PingMethod) {
            sendMessage(session, new PongReply(method.reqid));
        }

        //If the message is a join then find the client's match and check if they are already in the match.
        //If they aren't then add them to the match and respond with a JoinResponse.
        else if (method instanceof JoinMethod) {
            handleJoinMethod(session, (JoinMethod) method);
        }
        //If the message is a move event find the match that contains the client and play their move.
        else if (method instanceof MoveMethod) {
           handleMoveMethod(session, (MoveMethod) method);
        }
        //If we don't recognize the method type then respond with an UNKNOWN_MESSAGE error.
        else {
            sendMessage(session, new ErrorReply(Error.UNKNOWN_MESSAGE, method.reqid));
        }
    }

    private void handleJoinMethod(WebSocketSession session, JoinMethod joinMethod) throws IOException {
        boolean contains = false;
        synchronized (matchesMutex) {
            //Loop through each match in matches and find the one that allows the client.
            for (Map.Entry<String, Match> match : matches.entrySet()) {
                if (match.getValue().getAllowedClients().contains(joinMethod.clientUuid)) {
                    contains = true;
                    //Check if the client is already in a match. If so respond with a CLIENT_ALREADY_IN_MATCH error.
                    // Otherwise, add the client to the match and respond to inform the client.
                    if (match.getValue().containsClient(joinMethod.clientUuid)) {
                        sendMessage(session, new ErrorReply(Error.CLIENT_ALREADY_IN_MATCH, joinMethod.reqid));
                    }
                    else {
                        match.getValue().addClient(new Client(joinMethod.clientUuid, session));
                        sendMessage(session, new JoinReply(match.getValue().getUuid(), joinMethod.reqid));
                    }
                    break;
                }
            }
        }
        //If the client is not authorized to join a match then respond with a INVALID_CLIENT error.
        if (!contains) {
            sendMessage(session, new ErrorReply(Error.INVALID_CLIENT, joinMethod.reqid));
        }
    }

    private void handleMoveMethod(WebSocketSession session, MoveMethod moveMethod) throws IOException {
        boolean contains = false;
        synchronized (matchesMutex) {
            for (Map.Entry<String, Match> match : matches.entrySet()) {
                //If match contains client play the clients move.
                if (match.getValue().containsClientBySessionId(session.getId())) {
                    contains = true;
                    //Play the clients move. This also handles the game logic and sending out the results to the clients.
                    Match.Status status = match.getValue().playMove(session.getId(), moveMethod.move, moveMethod.reqid);
                    switch (status) {
                        case ERROR: //When the status is an error we need to inform the clients, close the connection, and remove the match.
                            match.getValue().getClients().forEach((c) -> {
                                try {
                                    sendMessage(c.getSession(), new ErrorReply(Error.MATCH_ERROR, moveMethod.reqid));
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
        //If the client is not authorized to play in the match then respond with a INVALID_CLIENT error.
        if (!contains) {
            sendMessage(session, new ErrorReply(Error.INVALID_CLIENT, moveMethod.reqid));
        }
    }

    /**
     * This helper function simplifies sending messages.
     *
     * @param session       WebSocket session to send the message on.
     * @param data          Data to send.
     * @throws IOException  Gets thrown whenever a message fails to send.
     */
    //Sends a message using session.
    private void sendMessage(WebSocketSession session, AbstractGameMessage data) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(data)));
    }
}
