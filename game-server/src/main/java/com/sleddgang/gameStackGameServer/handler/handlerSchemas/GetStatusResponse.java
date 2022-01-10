package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * GetStatusResponse is sent from the game handler to the matchmaking handler to give the number of running matches
 * and client queue size to a matchmaking server that requested them.
 *
 * @see GetStatusRequest
 * @author Benjamin
 */
@Getter
public class GetStatusResponse extends AbstractHandlerMessage {
    private final int gameSlotsLeft;
    private final int maxGameSlots;
    private final long clientQueue;

    public GetStatusResponse(int gameSlotsLeft, int maxGameSlots, long clientQueue, String server, long reqid) {
        super(server, reqid);
        this.gameSlotsLeft = gameSlotsLeft;
        this.maxGameSlots = maxGameSlots;
        this.clientQueue = clientQueue;
    }
}
