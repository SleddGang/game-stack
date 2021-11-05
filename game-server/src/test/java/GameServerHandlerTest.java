import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.Error;
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

    private WebSocketSession sendMessage(URI uri, GameServerMessage message, HandleText handleText) throws Exception {
        TextWebSocketHandler handler = createHandler(handleText);
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(this.objectMapper.writeValueAsBytes(message));
        session.sendMessage(socketMessage);
        return session;
    }

    private TextWebSocketHandler createHandler( HandleText handleText) {
        return new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                //Encode message as a string and add it to the blockingQueue.
                String m = new String(message.asBytes(), StandardCharsets.UTF_8);
                handleText.handleTextMessage(m);
            }
        };
    }

    @Test
    public void verifyPing() throws Exception{
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        WebSocketSession session = sendMessage(uri, new GameServerMessage(new PingEvent(), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof PongEvent, "The response event must be pong.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");

        session.close();
    }

    @Test
    public void verifyUnknownEvent() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        WebSocketSession session = sendMessage(uri, new GameServerMessage(new GameServerEvent("unknown"), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof ErrorEvent, "The response event must be error.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
        ErrorEvent error = (ErrorEvent) response.event;
        Assert.isTrue(error.error == Error.UNKNOWN_EVENT, "The response error type must be UNKNwON_EVENT");

        session.close();
    }

    @Test
    public void verifyInvalidReqid() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        TextWebSocketHandler handler = createHandler(message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new PingEvent(), reqid)));
        session.sendMessage(socketMessage);
        blockingQueue.poll(1, TimeUnit.SECONDS);
        session.sendMessage(socketMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof ErrorEvent, "The response event must be error.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
        ErrorEvent error = (ErrorEvent) response.event;
        Assert.isTrue(error.error == Error.INALID_REQID, "The response error type must be INVALID_REQID");

        session.close();
    }

    @Test
    public void verifyMatchmaking() throws Exception {
        WebSocketClient matchClient = new StandardWebSocketClient();

        String matchUuid = "test";
        String client1 = "client1";
        String client2 = "client2";

        long reqid = 1;
        long repeatReqid = reqid + 1;
        URI gameUri = new URI(String.format("ws://localhost:%d/game", this.port));
        URI matchUri = new URI(String.format("ws://localhost:%d/matchmaking", this.port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        TextWebSocketHandler handler = createHandler(message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });
        WebSocketSession gameSession = client.doHandshake(handler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClient.doHandshake(handler, null, matchUri).get(1, TimeUnit.SECONDS);

        TextMessage gameMessage = new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid)));
        TextMessage repeatGameMessage = new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), repeatReqid)));
        TextMessage matchMessage = new TextMessage(this.objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid)));
        matchSession.sendMessage(matchMessage);
        TimeUnit.SECONDS.sleep(1);
        gameSession.sendMessage(gameMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof JoinResponse, "The response event must be a JoinResponse.");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
        JoinResponse joinResponse = (JoinResponse) response.event;
        Assert.isTrue(joinResponse.matchUuid.equals(matchUuid), "The response match uuid must match the request.");

        //Resend the join message to check you can't join twice
        gameSession.sendMessage(repeatGameMessage);

        //Get the response from the blockingQueue and check it is correct.
        response = this.objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        Assert.isTrue(response.event instanceof ErrorEvent, "The response event must be a ErrorEvent.");
        Assert.isTrue(response.reqid == repeatReqid, "The response reqid must be the same as the request reqid.");
        ErrorEvent error = (ErrorEvent) response.event;
        Assert.isTrue(error.error == Error.CLIENT_ALREADY_IN_MATCH, "The response match error type must be CLIENT_ALREADY_IN_MATCH");

        gameSession.close();
        matchSession.close();
    }
}
