package com.sleddgang.gameStackGameServer.handler.shcemas;

import java.util.ArrayList;
import java.util.List;

public class Client extends Message {
    private String uuid;
    private String sessionId;


    public Client(String uuid, String sessionId) {
        this.uuid = uuid;
        this.sessionId = sessionId;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getSessionId() {
        return this.sessionId;
    }
}
