package com.sleddgang.gameStackGameServer.schemas;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @Type(value = PongEvent.class, name = "pong"),
        @Type(value = PingEvent.class, name = "ping"),
        @Type(value = ErrorEvent.class, name = "error"),
        @Type(value = JoinEvent.class, name = "join"),
        @Type(value = JoinResponse.class, name = "join_response"),
        @Type(value = CreateGameEvent.class, name = "create_game"),
})
public class GameServerEvent {

    public GameServerEvent(String type) {
    }

    public GameServerEvent() {
    }
}
