package com.sleddgang.gameStackGameServer.handler;

import java.util.ArrayList;
import java.util.Arrays;

public class Match {
    private final String uuid;
    private final ArrayList<Client> clients;
    private final ArrayList<String> allowedClients;

    Match(String uuid, String[] allowedClients) {
        this.uuid = uuid;
        this.clients = new ArrayList<>(2);
        this.allowedClients = new ArrayList<>(Arrays.asList(allowedClients));
    }

    void addClient(Client client) throws UnsupportedOperationException {
        if (this.clients.size() < 2) {
            if (this.allowedClients.contains(client.getUuid())) {
                this.clients.add(client);
            }
            else {
                throw new UnsupportedOperationException("Match " + this.uuid + " does not allow client " + client);
            }
        }
        else {
            throw new ArrayIndexOutOfBoundsException("Match " + this.uuid + " is already full");
        }
    }

    public String getUuid() {
        return this.uuid;
    }
    public ArrayList<String> getAllowedClients() {
        return this.allowedClients;
    }
    public ArrayList<Client> getClients() {
        return this.clients;
    }

    public boolean containsClient(String uuid) {
        for (Client client : this.clients) {
            if (client.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsClientBySessionId(String sessionId) {
        for (Client client : this.clients) {
            if (client.getSessionId().equals(sessionId)) {
                return true;
            }
        }
        return false;
    }

    public Client getClientBySessionId(String sessionId) {
        for (Client client : this.clients) {
            if (client.getSessionId().equals(sessionId)) {
                return client;
            }
        }
        return new Client("", "");
    }
}
