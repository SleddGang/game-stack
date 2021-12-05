package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Used to send the necessary information to create a match.
 * MatchMessage gets sent from the matchmaking handler to the game handler.
 *
 * @author Benjamin
 */
public class MatchMessage {
    private final String uuid;
    private final ArrayList<String> allowedClients;
    private final String server;
    private final long reqid;

    public MatchMessage(String uuid, String[] allowedClients, String server, long reqid) {
        this.uuid = uuid;
        this.allowedClients = new ArrayList<>(Arrays.asList(allowedClients));
        this.server = server;
        this.reqid = reqid;
    }

    public String getUuid() {
        return uuid;
    }

    public ArrayList<String> getAllowedClients() {
        return allowedClients;
    }

    public String getServer() {
        return server;
    }

    public long getReqid() {
        return reqid;
    }
}
