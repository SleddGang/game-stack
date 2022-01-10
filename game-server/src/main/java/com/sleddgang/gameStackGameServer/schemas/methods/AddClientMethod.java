package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;

/**
 * This method is sent from the matchmaking server to the game server to add a client to matchmaking.
 *
 * @see com.sleddgang.gameStackGameServer.schemas.replies.AddClientReply
 * @author Benjamin
 */
public class AddClientMethod extends AbstractGameMethod {
    /**
     * Uuid of the client that is being added.
     */
    public String clientUuid;

    public AddClientMethod(String clientUuid, long reqid) {
        this.clientUuid = clientUuid;
        this.reqid = reqid;
    }

    public AddClientMethod() {
        this("", 0);
    }
}
