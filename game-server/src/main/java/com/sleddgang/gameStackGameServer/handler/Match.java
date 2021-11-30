package com.sleddgang.gameStackGameServer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.gameLogic.CoreLogic;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.gameLogic.LogicResult;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.MatchMessage;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Message;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import com.sleddgang.gameStackGameServer.schemas.Result;
import com.sleddgang.gameStackGameServer.schemas.ResultEvent;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Match extends Message {
    private static int MAX_CLIENTS = 2;

    private final String uuid;
    //TODO This should maybe be a hash map.
    private final ArrayList<Client> clients;
    private final ArrayList<String> allowedClients;
    private final CoreLogic logic;

    public Match(String uuid, String[] allowedClients) {
        this.uuid = uuid;
        this.clients = new ArrayList<>(2);
        this.allowedClients = new ArrayList<>(Arrays.asList(allowedClients));
        this.logic = new CoreLogic();
    }

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

        Client client = getClientBySessionId(sessionId);
        if (client == null) {
            return Status.ERROR;
        }
        client.setMove(move, moveReqid);

        //Count how many clients have made a move;
        int validMoves = (int) clients.stream().filter(c -> c.getMove() != null).count();

        System.out.println(validMoves);

        if (validMoves == clients.size()) {
            LogicResult logicResult = logic.evaluate(clients.get(0).getMove(), clients.get(1).getMove());
            Result playerOneResult;
            Result playerTwoResult;
            switch (logicResult) {
                case PLAYERONE: {
                    playerOneResult = Result.WIN;
                    playerTwoResult = Result.LOSS;
                    break;
                }
                case PLAYERTWO: {
                    playerOneResult = Result.LOSS;
                    playerTwoResult = Result.WIN;
                    break;
                }
                default: {
                    playerOneResult = Result.TIE;
                    playerTwoResult = Result.TIE;
                }
            }

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

            return Status.ENDED;
        }

        return Status.PLAYING;
    }

    public void addClient(Client client) throws UnsupportedOperationException {
        if (clients.size() < MAX_CLIENTS) {
            if (allowedClients.contains(client.getUuid())) {
                clients.add(client);
            }
            else {
                throw new UnsupportedOperationException("Match " + uuid + " does not allow client " + client);
            }
        }
        else {
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

    public boolean containsClient(String uuid) {
        for (Client client : clients) {
            if (client.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return true;
            }
        }
        return false;
    }

    public Client getClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return client;
            }
        }
        return null;
    }

    public enum Status {
        JOINING,
        PLAYING,
        ENDED,
        ERROR;
    }
}
