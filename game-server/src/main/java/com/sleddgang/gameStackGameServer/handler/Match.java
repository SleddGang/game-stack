package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.gameLogic.CoreLogic;
import com.sleddgang.gameStackGameServer.gameLogic.LogicResult;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.AbstractHandlerMessage;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import com.sleddgang.gameStackGameServer.schemas.Result;
import com.sleddgang.gameStackGameServer.schemas.ResultEvent;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Match contains the information to keep track of a match as well as the logic to play the match.
 *
 * @see Client
 * @author Benjamin
 */
public class Match extends AbstractHandlerMessage {
    /**
     * Max number of allowed clients.
     */
    private static final int MAX_CLIENTS = 2;       //Max number of clients.

    /**
     * Uuid of the match given out by the matchmaking server.
     */
    private final String uuid;                      //Uuid of the match.

    /**
     * List of clients that are connected the the match
     */
    //TODO This should maybe be a hash map.
    private final ArrayList<Client> clients;        //List of clients that are connected to the match.

    /**
     * List of clients that are allowed to connect to the match.
     */
    private final ArrayList<String> allowedClients; //List of uuids of clients that are allowed to be in the match.

    /**
     * Game logic used to play rock paper scissors.
     */
    private final CoreLogic logic;                  //The core rock paper scissors logic.

    /**
     * This constructor creates a match based on a MatchMessage.
     *
     * @param matchMessage  Contains the uuid of the match and the list of allowed clients.
     */
    public Match(MatchMessage matchMessage) {
        this.uuid = matchMessage.getUuid();
        this.clients = new ArrayList<>(MAX_CLIENTS);
        this.allowedClients = matchMessage.getAllowedClients();
        this.logic = new CoreLogic();
    }

    /**
     * This function stores the move played by a client.
     * If both clients have played their moves then it will evaluate the game and
     * inform both clients of the result.
     * <p>See {@link com.sleddgang.gameStackGameServer.gameLogic.CoreLogic} for the actual rock paper scissors logic.</p>
     *
     * @param sessionId WebSocket session id of the client playing the move.
     * @param move      Move that the client is player. Either rock paper or scissors.
     * @param moveReqid Reqid of the client move message.
     * @return          Returns the status of the game.
     */
    public Status playMove(String sessionId, Option move, long moveReqid) {
        //Return if not all clients are in the match.
        if (clients.size() != MAX_CLIENTS) {
            return Status.JOINING;
        }

        //Return an error if the client is not already in the match.
        Client client = getClientBySessionId(sessionId);
        if (client == null) {
            return Status.ERROR;
        }
        client.setMove(move, moveReqid);

        //Count how many clients have made a move;
        int validMoves = (int) clients.stream().filter(c -> c.getMove() != null).count();

        //If all the connected players have made a move then evaluate the match.
        if (validMoves == clients.size()) {
            //Get the result of the match.
            LogicResult logicResult = logic.evaluate(clients.get(0).getMove(), clients.get(1).getMove());
            Result playerOneResult;
            Result playerTwoResult;
            switch (logicResult) {
                case PLAYERONE: {   //Player one wins.
                    playerOneResult = Result.WIN;
                    playerTwoResult = Result.LOSS;
                    break;
                }
                case PLAYERTWO: {   //Player two wins.
                    playerOneResult = Result.LOSS;
                    playerTwoResult = Result.WIN;
                    break;
                }
                default: {          //Tie
                    playerOneResult = Result.TIE;
                    playerTwoResult = Result.TIE;
                }
            }

            //Create a new object mapper to serialize the messages we are going to send to the clients.
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                clients.get(0).getSession().sendMessage(new TextMessage(objectMapper.writeValueAsBytes(
                        new GameServerMessage(new ResultEvent(playerOneResult), clients.get(0).getMoveReqid()))));
                clients.get(1).getSession().sendMessage(new TextMessage(objectMapper.writeValueAsBytes(
                        new GameServerMessage(new ResultEvent(playerTwoResult), clients.get(1).getMoveReqid()))));
            } catch (IOException e) {
                e.printStackTrace();
                return Status.ERROR;
            }

            //If the match has been evaluated and there hasn't been an error the match has ended.
            return Status.ENDED;
        }

        //If all the clients haven't made a move then the match is still in progress.
        return Status.PLAYING;
    }

    /**
     * Checks if the client is allowed in the match and that
     * the match is not full and if so adds the client to this match.
     *
     * @param client                            Client attempting to join the match.
     * @throws UnsupportedOperationException    Gets thrown if the client is not authorized to join this match.
     * @throws ArrayIndexOutOfBoundsException   Gets thrown if the match is full.
     */
    //Adds a client to the matches list of clients.
    public void addClient(Client client) throws UnsupportedOperationException, ArrayIndexOutOfBoundsException {
        //If we can fit a client in the match then add the client.
        if (clients.size() < MAX_CLIENTS) {
            //Check to see if the client is even allowed in the match.
            if (allowedClients.contains(client.getUuid())) {
                clients.add(client);
            }
            else {
                //This exception gets thrown whenever an attempt to add a client that is not authorized is made.
                throw new UnsupportedOperationException("Match " + uuid + " does not allow client " + client);
            }
        }
        else {
            //This exception gets thrown whenever the match already has the max number of clients.
            throw new ArrayIndexOutOfBoundsException("Match " + uuid + " is already full");
        }
    }

    /**
     * Disconnects the WebSockets of every client.
     */
    public void shutdown() {
        clients.forEach(client -> {
            try {
                client.getSession().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public String getUuid() {
        return uuid;
    }
    public ArrayList<String> getAllowedClients() {
        return allowedClients;
    }
    public ArrayList<Client> getClients() {
        return clients;
    }


    /**
     * Checks if the match contains a client based on the client's uuid
     *
     * @param uuid  Uuid of the client given from the matchmaking server.
     * @return      Returns true if the client is in this match and false if not.
     */
    //Check if the match contains a client based on the clients uuid.
    public boolean containsClient(String uuid) {
        for (Client client : clients) {
            if (client.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the match contains a client based on the client's WebSocket session id.
     *
     * @param sessionId WebSocket session id of the client's connection.
     * @return          Returns true if the client is in this match and false if not.
     */
    //Check if the match contains a client based on session id.
    public boolean containsClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the client based on the WebSocket session id.
     *
     * @param sessionId WebSocket session id of the client's connection.
     * @return          Returns the client if the client is found and null if the client is not found.
     */
    //Return a client based on session id.
    public Client getClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return client;
            }
        }
        return null;
    }

    /**
     * Used to inform the handler of the current state of the websocket
     * <p>JOINING if the clients are still joining.</p>
     * <p>PLAYING if only one client has played a move.</p>
     * <p>ENDED if the match has ended without error.</p>
     * <p>ERROR if the match encountered an error.</p>
     */
    //Used to inform the handler of the current status of the server.
    public enum Status {
        JOINING,
        PLAYING,
        ENDED,
        ERROR;
    }
}
