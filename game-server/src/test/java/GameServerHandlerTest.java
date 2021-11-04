import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.sleddgang.gameStackGameServer.GameServerApplication;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GameServerApplication.class)
class GameServerHandlerTest {

    private interface HandleText {
        void handleTextMessage(String message);
    }

    @LocalServerPort
    private Integer port;

    private WebSocketClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.objectMapper = new ObjectMapper();
        this.client = new StandardWebSocketClient();
    }

    private void sendMessage(URI uri, GameServerMessage message, HandleText handleText) throws Exception {
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                //Encode message as a string and add it to the blockingQueue.
                String m = new String(message.asBytes(), StandardCharsets.UTF_8);
                handleText.handleTextMessage(m);
            }
        };
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(this.objectMapper.writeValueAsBytes(message));
        session.sendMessage(socketMessage);
        session.close();
    }

    @Test
    public void verifyPing() throws Exception{
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        sendMessage(uri, new GameServerMessage(new PingEvent(), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof PongEvent, "The response event must be pong.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
    }

    @Test
    public void verifyUnknownEvent() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        sendMessage(uri, new GameServerMessage(new GameServerEvent("unknown"), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof ErrorEvent, "The response event must be error.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
        ErrorEvent error = (ErrorEvent) response.event;
        Assert.isTrue(error.id == 1, "The response error id must be 1.");
    }
}
