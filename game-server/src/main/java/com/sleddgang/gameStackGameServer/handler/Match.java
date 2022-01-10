package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.gameLogic.CoreLogic;
import com.sleddgang.gameStackGameServer.gameLogic.LogicResult;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.handler.handlerSchemas.AbstractHandlerMessage;
import com.sleddgang.gameStackGameServer.schemas.Result;
import com.sleddgang.gameStackGameServer.schemas.events.ResultEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Match contains the information to keep track of a match as well as the logic to play the match.
 *
 * @see Client
 * @author Benjamin
 */
@Log4j2
public class Match {
    /**
     * Max number of allowed clients.
     */
    private static final int MAX_CLIENTS = 2;       //Max number of clients.

    /**
     * Uuid of the match given out by the matchmaking server.
     */
    @Getter
    private final String uuid;                      //Uuid of the match.

    /**
     * List of clients that are connected to the match. The key is the clients session id.
     */
    @Getter
    private final Map<String, Client> clients;        //List of clients that are connected to the match.

    /**
     * Game logic used to play rock paper scissors.
     */
    private final CoreLogic logic;                  //The core rock paper scissors logic.

    public Match(String uuid, Map<String, Client> clients) {
        this.uuid = uuid;
        this.clients = clients;
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
     * Disconnects the WebSockets of every client.
     */
    public void shutdown() {
        log.debug("Shutting down match " + uuid);
        clients.values().forEach(client -> {
            try {
                if (client.getSession().isOpen()) {
                    client.getSession().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
