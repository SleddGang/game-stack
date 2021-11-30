import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.GameServerApplication;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@TestPropertySource(properties = {"ID = test1", "SLOTS = 2"})
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
        objectMapper = new ObjectMapper();
        client = new StandardWebSocketClient();
    }

    @Test
    public void verifyPing() throws Exception{
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        WebSocketSession session = sendMessage(uri, new GameServerMessage(new PingEvent(), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkMessage(response, reqid, PongEvent.class);

        session.close();
    }

    @Test
    public void verifyUnknownEvent() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        WebSocketSession session = sendMessage(uri, new GameServerMessage(new GameServerEvent(), reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(response, reqid, Error.UNKNOWN_EVENT);

        session.close();
    }

    @Test
    public void verifyInvalidReqid() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        TextWebSocketHandler handler = createHandler(message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new PingEvent(), reqid)));
        session.sendMessage(socketMessage);
        blockingQueue.poll(1, TimeUnit.SECONDS);
        session.sendMessage(socketMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(response, reqid, Error.INVALID_REQID);

        session.close();
    }

    @Test
    public void verifyMatchmaking() throws Exception {
        WebSocketClient matchClient = new StandardWebSocketClient();

        String matchUuid = "test1";
        String client1 = "client1";
        String client2 = "client2";

        long reqid = 1;
        long repeatReqid = reqid + 1;
        URI gameUri = new URI(String.format("ws://localhost:%d/game", port));
        URI matchUri = new URI(String.format("ws://localhost:%d/matchmaking", port));
        BlockingQueue<String> gameBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.
        BlockingQueue<String> matchBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        TextWebSocketHandler gameHandler = createHandler(message -> {
            System.out.println("Game Handler: " + message);
            gameBlockingQueue.add(message);
        });

        TextWebSocketHandler matchHandler = createHandler(message -> {
            System.out.println("Match Handler: " + message);
            matchBlockingQueue.add(message);
        });

        WebSocketSession gameSession = client.doHandshake(gameHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClient.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        TextMessage gameMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid)));
        TextMessage repeatGameMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), repeatReqid)));
        TextMessage matchMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid)));
        matchSession.sendMessage(matchMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid);

        gameSession.sendMessage(gameMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage gameResponse = objectMapper.readValue(gameBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameResponse, reqid, matchUuid);

        //Resend the join message to check you can't join twice
        gameSession.sendMessage(repeatGameMessage);

        //Get the response from the blockingQueue and check it is correct.
        gameResponse = objectMapper.readValue(gameBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(gameResponse, repeatReqid, Error.CLIENT_ALREADY_IN_MATCH);

        gameSession.close();
        matchSession.close();
    }

    @Test
    public void verifyGame() throws Exception {
        WebSocketClient clientTwoSocket = new StandardWebSocketClient();
        WebSocketClient matchClientSocket = new StandardWebSocketClient();

        String matchUuid = "test2";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;
        URI gameUri = new URI(String.format("ws://localhost:%d/game", port));
        URI matchUri = new URI(String.format("ws://localhost:%d/matchmaking", port));
        BlockingQueue<String> clientOneBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.
        BlockingQueue<String> clientTwoBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.
        BlockingQueue<String> matchBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        TextWebSocketHandler clientOneHandler = createHandler(message -> {
            System.out.println("Client1 Handler: " + message);
            clientOneBlockingQueue.add(message);
        });
        TextWebSocketHandler clientTwoHandler = createHandler(message -> {
            System.out.println("Client2 Handler: " + message);
            clientTwoBlockingQueue.add(message);
        });
        TextWebSocketHandler matchHandler = createHandler(message -> {
            System.out.println("Match Handler: " + message);
            matchBlockingQueue.add(message);
        });

        WebSocketSession gameOneSession = client.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client2), reqid))));

        GameServerMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        GameServerMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameOneResponse, reqid, matchUuid);
        checkJoinResponse(gameTwoResponse, reqid, matchUuid);

        reqid++;

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new MoveEvent(Option.ROCK), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new MoveEvent(Option.PAPER), reqid))));

        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkResult(gameOneResponse, reqid, Result.LOSS);
        checkResult(gameTwoResponse, reqid, Result.WIN);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    private WebSocketSession sendMessage(URI uri, GameServerMessage message, HandleText handleText) throws Exception {
        TextWebSocketHandler handler = createHandler(handleText);
        WebSocketSession session = client.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(message));
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

    private void checkMessage(GameServerMessage response, long reqid, Class<?> type) {
        Assert.isTrue(type.isInstance(response.event), "The response event must be a " + type + ".");
        Assert.isTrue(response.reqid == reqid, "The response reqid must be the same as the request reqid.");
    }

    private void checkJoinResponse(GameServerMessage response, long reqid, String matchUuid) {
        checkMessage(response, reqid, JoinResponse.class);
        JoinResponse joinResponse = (JoinResponse) response.event;
        Assert.isTrue(joinResponse.matchUuid.equals(matchUuid), "The response match uuid must match the request.");
    }

    private void checkStatus(GameServerMessage response, long reqid) {
        checkMessage(response, 0, ServerStatusEvent.class);
        ServerStatusEvent serverStatusEvent = (ServerStatusEvent) response.event;
        Assert.isTrue(serverStatusEvent.slotsLeft == 1, "Slots left must be 1.");
    }

    private void checkResult(GameServerMessage response, long reqid, Result result) {
        checkMessage(response, reqid, ResultEvent.class);
        ResultEvent resultEvent = (ResultEvent) response.event;
        Assert.isTrue(resultEvent.result == result, "Unexpected result.");
    }

    private void checkError(GameServerMessage response, long reqid, Error error) {
        checkMessage(response, reqid, ErrorEvent.class);
        ErrorEvent errorEvent = (ErrorEvent) response.event;
        Assert.isTrue(errorEvent.error == error, "Error must be of type " + error + ".");
    }
}
