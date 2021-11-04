import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.GameServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameServerHandlerTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private Integer port;

    private WebSocketClient client;

    @BeforeEach
    public void setup() {
        this.client = new StandardWebSocketClient();
    }

    @Test
    public void verifyPing() throws Exception{
        long reqid = Long.MAX_VALUE;
        URI uri = new URI(" ws://localhost:8080/game");
        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {

            }

            @Override
            public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {

            }

            @Override
            public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {

            }

            @Override
            public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {

            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);
        GameServerMessage message = new GameServerMessage("ping", reqid);
        WebSocketMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(message));

    }
}
