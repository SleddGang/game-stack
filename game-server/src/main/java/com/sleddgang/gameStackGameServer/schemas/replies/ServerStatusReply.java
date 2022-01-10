package com.sleddgang.gameStackGameServer.schemas.replies;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;

/**
 * ServerStatusReply is sent from the game server to the matchmaking server to
 */
public class ServerStatusReply extends AbstractGameReply {
    /**
     * Number of match slots left.
     */
    public int gameSlotsLeft;

    /**
     * Total number of match slots.
     */
    public int maxGameSlots;

    /**
     * Number of clients in queue for a match.
     */
    public long clientQueue;

    public ServerStatusReply(int gameSlotsLeft, int maxGameSlots, long clientQueue, long id) {
        this.gameSlotsLeft = gameSlotsLeft;
        this.maxGameSlots = maxGameSlots;
        this.clientQueue = clientQueue;
        this.id = id;
    }

    public ServerStatusReply() {
        this.gameSlotsLeft = 0;
        this.maxGameSlots = 0;
        this.clientQueue = 0;
        this.id = 0;
    }
}
