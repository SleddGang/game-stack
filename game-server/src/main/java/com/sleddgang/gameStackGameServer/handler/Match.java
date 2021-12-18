package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.gameLogic.CoreLogic;
import com.sleddgang.gameStackGameServer.gameLogic.LogicResult;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.AbstractHandlerMessage;
import com.sleddgang.gameStackGameServer.schemas.Result;
import com.sleddgang.gameStackGameServer.schemas.events.ResultEvent;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * List of clients that are connected to the match. The key is the clients session id.
     */
    private final Map<String, Client> clients;        //List of clients that are connected to the match.

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
        this.clients = new HashMap<>(MAX_CLIENTS);
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
     * @return          Returns the status of the game.
     */
    public MatchStatus playMove(String sessionId, Option move) {
        //Return if not all clients are in the match.
        if (clients.size() != MAX_CLIENTS) {
            return MatchStatus.JOINING;
        }

        //Return if we don't recognize the session id as a clients.
        if (!clients.containsKey(sessionId)) {
            return MatchStatus.ERROR;
        }
        Client client = clients.get(sessionId);

        client.setMove(move);

        //Count how many clients have made a move;
        int validMoves = (int) clients.values().stream().filter(c -> c.getMove() != null).count();

        //If all the connected players have made a move then evaluate the match.
        if (validMoves == clients.size()) {
            //Get the result of the match.
            List<Client> numberedClients = new ArrayList<>(clients.values());
            LogicResult logicResult = logic.evaluate(numberedClients.get(0).getMove(), numberedClients.get(1).getMove());
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
                numberedClients.get(0).getSession().sendMessage(new TextMessage(objectMapper.writeValueAsBytes(
                        new ResultEvent(playerOneResult))));
                numberedClients.get(1).getSession().sendMessage(new TextMessage(objectMapper.writeValueAsBytes(
                        new ResultEvent(playerTwoResult))));
            } catch (IOException e) {
                e.printStackTrace();
                return MatchStatus.ERROR;
            }

            //If the match has been evaluated and there hasn't been an error the match has ended.
            return MatchStatus.ENDED;
        }

        //If all the clients haven't made a move then the match is still in progress.
        return MatchStatus.PLAYING;
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
                clients.put(client.getSessionId(), client);
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
        clients.values().forEach(client -> {
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
    public Map<String, Client> getClients() {
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
        return clients.values().stream().anyMatch(client -> client.getUuid().equals(uuid));
    }
}
