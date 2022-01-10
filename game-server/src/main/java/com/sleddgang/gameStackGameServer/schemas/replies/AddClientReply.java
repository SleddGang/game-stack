package com.sleddgang.gameStackGameServer.schemas.replies;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;

/**
 * This reply is sent from the game server to the matchmaking server to
 * inform it that a client was successfully added to matchmaking.
 *
 * @see com.sleddgang.gameStackGameServer.schemas.methods.AddClientMethod
 * @author Benjamin
 */
public class AddClientReply extends AbstractGameReply {
    /**
     * Uuid of the client that was added to the server.
     */
    public String clientUuid;

    public AddClientReply(String clientUuid, long reqid) {
        this.clientUuid = clientUuid;
        this.id = reqid;
    }

    public AddClientReply() {
        this("", 0);
    }
}
