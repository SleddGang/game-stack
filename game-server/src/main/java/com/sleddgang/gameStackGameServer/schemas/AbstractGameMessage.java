package com.sleddgang.gameStackGameServer.schemas;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sleddgang.gameStackGameServer.schemas.events.ResultEvent;
import com.sleddgang.gameStackGameServer.schemas.events.ServerStatusEvent;
import com.sleddgang.gameStackGameServer.schemas.methods.*;
import com.sleddgang.gameStackGameServer.schemas.replies.*;

/**
 * WebSocket message sent between either a client or a matchmaking server and the game server.
 *
 * @author Benjamin
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AbstractGameMethod.class, name = "method"),
        @JsonSubTypes.Type(value = PongReply.class, name = "pong"),
        @JsonSubTypes.Type(value = PingMethod.class, name = "ping"),
        @JsonSubTypes.Type(value = ErrorReply.class, name = "error"),
        @JsonSubTypes.Type(value = JoinMethod.class, name = "join"),
        @JsonSubTypes.Type(value = JoinReply.class, name = "join_response"),
        @JsonSubTypes.Type(value = AddClientMethod.class, name = "add_client"),
        @JsonSubTypes.Type(value = AddClientReply.class, name = "add_client_reply"),
        @JsonSubTypes.Type(value = GetStatusMethod.class, name = "get_server_status"),
        @JsonSubTypes.Type(value = ServerStatusReply.class, name = "server_status"),
        @JsonSubTypes.Type(value = MoveMethod.class, name = "move"),
        @JsonSubTypes.Type(value = ResultEvent.class, name = "result"),
})
public class AbstractGameMessage {
}
