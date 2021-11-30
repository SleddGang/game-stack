package com.sleddgang.gameStackGameServer.handler.handlerShcemas;

import java.util.ArrayList;
import java.util.Arrays;

public class MatchMessage {
    private final String uuid;
    private final ArrayList<String> allowedClients;

    public MatchMessage(String uuid, String[] allowedClients) {
        this.uuid = uuid;
        this.allowedClients = new ArrayList<>(Arrays.asList(allowedClients));
    }

    public String getUuid() {
        return uuid;
    }

    public ArrayList<String> getAllowedClients() {
        return allowedClients;
    }
}
