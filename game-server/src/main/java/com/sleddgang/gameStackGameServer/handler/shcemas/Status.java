package com.sleddgang.gameStackGameServer.handler.shcemas;

public class Status extends Message {
    private final int slots;

    public Status(int slots) {
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }
}
