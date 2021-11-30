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
import java.net.URISyntaxException;
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

    private WebSocketClient clientOneSocket;
    private WebSocketClient clientTwoSocket;
    private WebSocketClient matchClientSocket;
    private ObjectMapper objectMapper;
    private URI gameUri;
    private URI matchUri;
    private BlockingQueue<String> clientOneBlockingQueue;
    private BlockingQueue<String> clientTwoBlockingQueue;
    private BlockingQueue<String> matchBlockingQueue;
    private TextWebSocketHandler clientOneHandler;
    private TextWebSocketHandler clientTwoHandler;
    private TextWebSocketHandler matchHandler;

    @BeforeEach
    public void setup() throws URISyntaxException {
        objectMapper = new ObjectMapper();
        clientOneSocket = new StandardWebSocketClient();
        clientTwoSocket = new StandardWebSocketClient();
        matchClientSocket = new StandardWebSocketClient();

        gameUri = new URI(String.format("ws://localhost:%d/game", port));
        matchUri = new URI(String.format("ws://localhost:%d/matchmaking", port));
        clientOneBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.
        clientTwoBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.
        matchBlockingQueue = new ArrayBlockingQueue(2);    //Used to send messages out of the handler.

        clientOneHandler = createHandler(message -> {
            System.out.println("Client1 Handler: " + message);
            clientOneBlockingQueue.add(message);
        });
        clientTwoHandler = createHandler(message -> {
            System.out.println("Client2 Handler: " + message);
            clientTwoBlockingQueue.add(message);
        });
        matchHandler = createHandler(message -> {
            System.out.println("Match Handler: " + message);
            matchBlockingQueue.add(message);
        });
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

        WebSocketSession session = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);


        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new PingEvent(), reqid)));
        session.sendMessage(socketMessage);
        clientOneBlockingQueue.poll(1, TimeUnit.SECONDS);
        session.sendMessage(socketMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage response = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(response, reqid, Error.INVALID_REQID);

        session.close();
    }

    @Test
    public void verifyMatchmaking() throws Exception {
        String matchUuid = "test1";
        String client1 = "client1";
        String client2 = "client2";

        long reqid = 1;
        long repeatReqid = reqid + 1;

        TextWebSocketHandler gameHandler = createHandler(message -> {
            System.out.println("Game Handler: " + message);
            clientOneBlockingQueue.add(message);
        });

        TextWebSocketHandler matchHandler = createHandler(message -> {
            System.out.println("Match Handler: " + message);
            matchBlockingQueue.add(message);
        });

        WebSocketSession gameSession = clientOneSocket.doHandshake(gameHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        TextMessage gameMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid)));
        TextMessage repeatGameMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), repeatReqid)));
        TextMessage matchMessage = new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid)));
        matchSession.sendMessage(matchMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid, 1);

        gameSession.sendMessage(gameMessage);

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage gameResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameResponse, reqid, matchUuid);

        //Resend the join message to check you can't join twice
        gameSession.sendMessage(repeatGameMessage);

        //Get the response from the blockingQueue and check it is correct.
        gameResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(gameResponse, repeatReqid, Error.CLIENT_ALREADY_IN_MATCH);

        gameSession.close();
        matchSession.close();
    }

    @Test
    public void verifyMatchmakingErrors() throws Exception {
        String matchUuid = "test4";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid, 1);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(matchResponse, reqid, Error.DUPLICATE_MATCH);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid))));

        GameServerMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameOneResponse, reqid, matchUuid);

        gameOneSession.close();
        TimeUnit.MILLISECONDS.sleep(250);
        Assert.isTrue(!gameOneSession.isOpen(), "GameOneSession should not be open");

        reqid++;

        for (int i = 0; i < 3; i++) {
            System.out.println("Creating match: " + i);
            matchUuid = "test4-" + i;
            client1 = "gameClient1-" + i;
            client2 = "gameClient2-" + i;

            matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid + i))));
        }

        //Get the response from the blockingQueue and check it is correct.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, 0, 1);
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, 0, 0);
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkError(matchResponse, reqid + 2, Error.MATCHES_FULL);

        matchSession.close();
        gameOneSession.close();
    }

    @Test
    public void verifyShutdown() throws Exception {
        String matchUuid = "test3";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid, 1);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client2), reqid))));

        GameServerMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        GameServerMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameOneResponse, reqid, matchUuid);
        checkJoinResponse(gameTwoResponse, reqid, matchUuid);

        reqid++;

        gameOneSession.close();
        TimeUnit.MILLISECONDS.sleep(250);
        Assert.isTrue(!gameOneSession.isOpen(), "GameOneSession should not be open");
        Assert.isTrue(!gameTwoSession.isOpen(), "GameTwoSession should not be open");

        gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
         matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid, 1);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client2), reqid))));

        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameOneResponse, reqid, matchUuid);
        checkJoinResponse(gameTwoResponse, reqid, matchUuid);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    @Test
    public void verifyGame() throws Exception {
        String matchUuid = "test2";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new CreateGameEvent(matchUuid, new String[]{client1, client2}), reqid))));

        //Get the response from the blockingQueue and check it is correct.
        GameServerMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), GameServerMessage.class);
        checkStatus(matchResponse, reqid, 1);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client1), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new JoinEvent(client2), reqid))));

        GameServerMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        GameServerMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkJoinResponse(gameOneResponse, reqid, matchUuid);
        checkJoinResponse(gameTwoResponse, reqid, matchUuid);

        reqid++;

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new MoveEvent(Option.ROCK), reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GameServerMessage(new MoveEvent(Option.PAPER), reqid))));

        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), GameServerMessage.class);
        checkResult(gameOneResponse, reqid, Result.LOSS);
        checkResult(gameTwoResponse, reqid, Result.WIN);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    private WebSocketSession sendMessage(URI uri, GameServerMessage message, HandleText handleText) throws Exception {
        TextWebSocketHandler handler = createHandler(handleText);
        WebSocketSession session = clientOneSocket.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

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

    private void checkStatus(GameServerMessage response, long reqid, int slots) {
        checkMessage(response, 0, ServerStatusEvent.class);
        ServerStatusEvent serverStatusEvent = (ServerStatusEvent) response.event;
        Assert.isTrue(serverStatusEvent.slotsLeft == slots, "Slots left must be 1.");
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
