package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.gameLogic.CoreLogic;
import com.sleddgang.gameStackGameServer.gameLogic.LogicResult;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Message;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import com.sleddgang.gameStackGameServer.schemas.Result;
import com.sleddgang.gameStackGameServer.schemas.ResultEvent;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.ArrayList;

public class Match extends Message {
    private static final int MAX_CLIENTS = 2;       //Max number of clients.

    private final String uuid;                      //Uuid of the match.
    //TODO This should maybe be a hash map.
    private final ArrayList<Client> clients;        //List of clients that are connected to the match.
    private final ArrayList<String> allowedClients; //List of uuids of clients that are allowed to be in the match.
    private final CoreLogic logic;                  //The core rock paper scissors logic.

    public Match(MatchMessage matchMessage) {
        this.uuid = matchMessage.getUuid();
        this.clients = new ArrayList<>(MAX_CLIENTS);
        this.allowedClients = matchMessage.getAllowedClients();
        this.logic = new CoreLogic();
    }

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

    //Adds a client to the matches list of clients.
    public void addClient(Client client) throws UnsupportedOperationException {
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

    public String getUuid() {
        return uuid;
    }
    public ArrayList<String> getAllowedClients() {
        return allowedClients;
    }
    public ArrayList<Client> getClients() {
        return clients;
    }

    //Check if the match contains a client based on the clients uuid.
    public boolean containsClient(String uuid) {
        for (Client client : clients) {
            if (client.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    //Check if the match contains a client based on session id.
    public boolean containsClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return true;
            }
        }
        return false;
    }

    //Return a client based on session id.
    public Client getClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return client;
            }
        }
        return null;
    }

    //Used to inform the handler of the current status of the server.
    public enum Status {
        JOINING,
        PLAYING,
        ENDED,
        ERROR;
    }
}
