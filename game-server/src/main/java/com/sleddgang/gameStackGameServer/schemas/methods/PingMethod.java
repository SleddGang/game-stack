package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.replies.PongReply;
import org.springframework.web.socket.PingMessage;

/**
 * PingEvent is sent by a client to the game server in order to test the connection.
 *
 * @see PongReply
 * @author Benjamin
 */
public class PingMethod extends AbstractGameMethod {
    public PingMethod(long reqid) {
        this.reqid = reqid;
    }

    public PingMethod() {
        this(0);
    }
}
