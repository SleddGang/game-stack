package com.sleddgang.gameStackGameServer.schemas.events;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameEvent;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;

/**
 * This event is sent from a game server to a matchmaking server to inform the matchmaking server of the number of
 * slots left on the game server.
 *
 * @author Benjamin
 */
public class ServerStatusReply extends AbstractGameEvent {
    public String uuid;
    public int slotsLeft;

    public ServerStatusReply(String uuid, int slotsLeft) {
        this.uuid = uuid;
        this.slotsLeft = slotsLeft;
    }

    public ServerStatusReply() {
        this("", -1);
    }
}
