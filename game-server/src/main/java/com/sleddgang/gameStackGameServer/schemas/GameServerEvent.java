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
})
public class GameServerEvent {
//    public String type;

    public GameServerEvent(String type) {
//        this.type = type;
    }

    public GameServerEvent() {
    }
}
