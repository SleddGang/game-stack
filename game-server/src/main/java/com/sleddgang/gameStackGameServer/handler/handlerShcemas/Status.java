package com.sleddgang.gameStackGameServer.handler.handlerShcemas;

/**
 * Used to inform the matchmaking servers about how many slots are open on this game server.
 * Gets sent from the game handler to the matchmaking handler.
 *
 * @author Benjamin
 */
public class Status extends AbstractHandlerMessage {
    private final int slots;

    public Status(int slots) {
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }
}
