package com.sleddgang.gameStackGameServer.handler.shcemas;

import java.util.ArrayList;
import java.util.Arrays;

public class Match extends Message {
    private final String uuid;
    private final ArrayList<Client> clients;
    private final ArrayList<String> allowedClients;

    public Match(String uuid, String[] allowedClients) {
        this.uuid = uuid;
        this.clients = new ArrayList<>(2);
        this.allowedClients = new ArrayList<>(Arrays.asList(allowedClients));
    }

    public void addClient(Client client) throws UnsupportedOperationException {
        if (clients.size() < 2) {
            if (allowedClients.contains(client.getUuid())) {
                clients.add(client);
            }
            else {
                throw new UnsupportedOperationException("Match " + uuid + " does not allow client " + client);
            }
        }
        else {
            throw new ArrayIndexOutOfBoundsException("Match " + uuid + " is already full");
        }
    }

    public String getUuid() {
        return uuid;
    }
    public ArrayList<String> getAllowedClients() {
        return allowedClients;
    }
    public ArrayList<Client> getClients() {
        return clients;
    }

    public boolean containsClient(String uuid) {
        for (Client client : clients) {
            if (client.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return true;
            }
        }
        return false;
    }

    public Client getClientBySessionId(String sessionId) {
        for (Client client : clients) {
            if (client.getSessionId().equals(sessionId)) {
                return client;
            }
        }
        return new Client("", "");
    }
}
