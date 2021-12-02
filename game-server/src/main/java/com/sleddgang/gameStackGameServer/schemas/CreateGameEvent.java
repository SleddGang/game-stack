package com.sleddgang.gameStackGameServer.schemas;

/**
 * This event is sent from the matchmaking server to the game server to inform it that it should make a new match.
 *
 * @author Benjamin
 */
public class CreateGameEvent extends GameServerEvent {
    /**
     * Uuid of the match to create.
     */
    public String uuid;

    /**
     * Clients that are allowed to join this match.
     */
    public String[] clients;

    public CreateGameEvent(String uuid, String[] clients) {
        this.uuid = uuid;
        this.clients = clients;
    }

    public CreateGameEvent() {
        this.uuid = "";
        this.clients = new String[2];
    }
}
