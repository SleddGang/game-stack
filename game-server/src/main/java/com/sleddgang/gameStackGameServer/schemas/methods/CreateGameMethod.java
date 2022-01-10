package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;

/**
 * This method is sent from the matchmaking server to the game server to inform it that it should make a new match.
 *
 * @author Benjamin
 */
public class CreateGameMethod extends AbstractGameMethod {
    /**
     */
    public String uuid;

    /**
     * Clients that are allowed to join this match.
     */
    public String[] clients;

    public CreateGameMethod(String uuid, String[] clients, long reqid) {
        this.uuid = uuid;
        this.clients = clients;
        this.reqid = reqid;
    }

    public CreateGameMethod() {
        this("", new String[2], 0);
    }
}
