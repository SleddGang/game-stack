package com.sleddgang.gameStackGameServer.schemas;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Parent of every event.
 *
 * @author Benjamin
 */
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
        @Type(value = ServerStatusEvent.class, name = "server_status"),
        @Type(value = MoveEvent.class, name = "move"),
        @Type(value = ResultEvent.class, name = "result"),
})
public class GameServerEvent {

    public GameServerEvent() {
    }
}
