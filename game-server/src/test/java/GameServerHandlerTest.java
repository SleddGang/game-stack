import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.GameServerApplication;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.events.ResultEvent;
import com.sleddgang.gameStackGameServer.schemas.events.ServerStatusReply;
import com.sleddgang.gameStackGameServer.schemas.methods.*;
import com.sleddgang.gameStackGameServer.schemas.replies.ErrorReply;
import com.sleddgang.gameStackGameServer.schemas.replies.JoinReply;
import com.sleddgang.gameStackGameServer.schemas.replies.PongReply;
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

    //Verifies that if we send the server a ping message the server will respond with a pong.
    @Test
    public void verifyPing() throws Exception{
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        //Send our message.
        WebSocketSession session = sendMessage(uri, new PingMethod(reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and verify that the reqid is correct and that it is a pong message.
        AbstractGameMessage response = objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkReply(response, reqid, PongReply.class);

        session.close();
    }

    //Verifies that if we send an event that the server does not recognize that the server will respond with an UNKNOWN_EVENT error.
    @Test
    public void verifyUnknownEvent() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();
        URI uri = new URI(String.format("ws://localhost:%d/game", port));
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);    //Used to send messages out of the handler.

        //Send our ping message.
        WebSocketSession session = sendMessage(uri, new AbstractGameMethod(reqid), message -> {
            System.out.println(message);
            blockingQueue.add(message);
        });

        //Get the response from the blockingQueue and verify that the reqid is correct and that it is an UNKNOWN_EVENT error.
        AbstractGameMessage response = objectMapper.readValue(blockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(response, reqid, Error.UNKNOWN_MESSAGE);

        session.close();
    }

    //Verifies that if we send two messages to the server with the same reqid the server will respond with an INVALID_REQID error.
    @Test
    public void verifyInvalidReqid() throws Exception {
        Random rand = new Random();
        long reqid = rand.nextLong();

        WebSocketSession session = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(new PingMethod(reqid)));
        session.sendMessage(socketMessage);
        clientOneBlockingQueue.poll(1, TimeUnit.SECONDS);

        //Send the same message again.
        session.sendMessage(socketMessage);

        //Get the response from the blockingQueue and verify that the reqid is correct and that it is an INVALID_REQID error.
        AbstractGameMessage response = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(response, reqid, Error.INVALID_REQID);

        session.close();
    }

    //Verifies that we are able to make a new match and that both clients are able to join.
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

        TextMessage gameMessage = new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid)));
        TextMessage repeatGameMessage = new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, repeatReqid)));
        TextMessage matchMessage = new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid)));
        matchSession.sendMessage(matchMessage);

        //Get the response from the blockingQueue and check it is correct.
        AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);

        gameSession.sendMessage(gameMessage);

        //Get the response from the blockingQueue and check it is correct.
        AbstractGameMessage gameResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameResponse, reqid, matchUuid);

        //Resend the join message to check you can't join twice
        gameSession.sendMessage(repeatGameMessage);

        //Get the response from the blockingQueue and check it is correct.
        gameResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(gameResponse, repeatReqid, Error.CLIENT_ALREADY_IN_MATCH);

        gameSession.close();
        matchSession.close();
    }

    //Verifies that a matchmaking server can't make a match with with an id that is in use and that the game server will
    // inform the matchmaking server if it can't make a new match because it is full.
    @Test
    public void verifyMatchmakingErrors() throws Exception {
        String matchUuid = "test4";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        //Create a match.
        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid))));

        //Get the response from the blockingQueue and check it is correct.
        AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);

        //Try and create the same match again.
        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid))));

        //Get the response from the blockingQueue and check it is correct.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(matchResponse, reqid, Error.DUPLICATE_MATCH);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid))));

        //Join the match as a client and then disconnect because that's the only way I have made a way to shut down a game.
        AbstractGameMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameOneResponse, reqid, matchUuid);

        gameOneSession.close();
        TimeUnit.MILLISECONDS.sleep(250);
        Assert.isTrue(!gameOneSession.isOpen(), "GameOneSession should not be open");

        reqid++;

        //Because the game server is set to have a maximum of two matches we create two matches and verify we can't create a third.
        for (int i = 0; i < 3; i++) {
            System.out.println("Creating match: " + i);
            matchUuid = "test4-" + i;
            client1 = "gameClient1-" + i;
            client2 = "gameClient2-" + i;

            matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid + i))));
        }

        //Get the response from the blockingQueue and check it is correct.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 0);
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(matchResponse, reqid + 2, Error.MATCHES_FULL);

        matchSession.close();
        gameOneSession.close();
    }

    //Verifies that when one client disconnects the onther client will be disconnected.
    @Test
    public void verifyShutdown() throws Exception {
        String matchUuid = "test3";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid))));

        //Get the response from the blockingQueue and check it is correct.
        AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client2, reqid))));

        AbstractGameMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        AbstractGameMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameOneResponse, reqid, matchUuid);
        checkJoinReply(gameTwoResponse, reqid, matchUuid);

        reqid++;

        gameOneSession.close();
        TimeUnit.MILLISECONDS.sleep(250);
        Assert.isTrue(!gameOneSession.isOpen(), "GameOneSession should not be open");
        Assert.isTrue(!gameTwoSession.isOpen(), "GameTwoSession should not be open");

        //Recreate the same match to ensure it is shut down.
        gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);

        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid))));

        //Get the response from the blockingQueue and check it is correct.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);

        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client2, reqid))));

        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameOneResponse, reqid, matchUuid);
        checkJoinReply(gameTwoResponse, reqid, matchUuid);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    //Verifies the game logic by having two clients play joining and playing and check to make sure they win/lose correctly.
    @Test
    public void verifyGame() throws Exception {
        String matchUuid = "test2";
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        //Create the match.
        matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new CreateGameMethod(matchUuid, new String[]{client1, client2}, reqid))));

        AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkStatus(matchResponse, 1);

        //Both client's join the match.
        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client2, reqid))));

        AbstractGameMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        AbstractGameMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameOneResponse, reqid, matchUuid);
        checkJoinReply(gameTwoResponse, reqid, matchUuid);

        reqid++;

        //Client one plays rock and client two plays paper so client two should win and client one should lose.
        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new MoveMethod(Option.ROCK, reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new MoveMethod(Option.PAPER, reqid))));

        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkResult(gameOneResponse, reqid, Result.LOSS);
        checkResult(gameTwoResponse, reqid, Result.WIN);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    //Helper function to easily send one message. Cannot send a message over the same session again.
    private WebSocketSession sendMessage(URI uri, AbstractGameMessage message, HandleText handleText) throws Exception {
        TextWebSocketHandler handler = createHandler(handleText);
        WebSocketSession session = clientOneSocket.doHandshake(handler, null, uri).get(1, TimeUnit.SECONDS);

        //Create ping message to send to the server using the random reqid and send it to the server.
        TextMessage socketMessage = new TextMessage(objectMapper.writeValueAsBytes(message));
        session.sendMessage(socketMessage);
        return session;
    }

    //Helper function to easily create a handler for listening for messages.
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

    //Checks that the reply reqid is correct and that the reply is of the correct type.
    private void checkReply(AbstractGameMessage response, long reqid, Class<?> type) {
        Assert.isTrue(type.isInstance(response), "The response event must be a " + type + ".");
        AbstractGameReply reply = (AbstractGameReply) response;
        Assert.isTrue(reply.id == reqid, "The response reqid must be the same as the request reqid.");
    }

    //Calls checkMessage and verifies that the match uuid is correct.
    private void checkJoinReply(AbstractGameMessage response, long reqid, String matchUuid) {
        checkReply(response, reqid, JoinReply.class);
        JoinReply joinReply = (JoinReply) response;
        Assert.isTrue(joinReply.matchUuid.equals(matchUuid), "The response match uuid must match the request.");
    }

    //Checks that the response is a ServerStatusReply and verifies that the slots left is correct.
    private void checkStatus(AbstractGameMessage response, int slots) {
        Assert.isTrue(response instanceof ServerStatusReply, "The response must be of type ServerStatusReply");
        ServerStatusReply serverStatusReply = (ServerStatusReply) response;
        Assert.isTrue(serverStatusReply.slotsLeft == slots, "Slots left must be 1.");
    }

    //Cheks that the response is a ResultEvent and verifies that the result is correct.
    private void checkResult(AbstractGameMessage response, long reqid, Result result) {
        Assert.isTrue(response instanceof ResultEvent, "The response must be of type ResultEvent");
        ResultEvent resultReply = (ResultEvent) response;
        Assert.isTrue(resultReply.result == result, "Unexpected result.");
    }

    //Calls checkMessage and verifies that the error is the correct type.
    private void checkError(AbstractGameMessage response, long reqid, Error error) {
        checkReply(response, reqid, ErrorReply.class);
        ErrorReply errorReply = (ErrorReply) response;
        Assert.isTrue(errorReply.error == error, "Error must be of type " + error + ".");
    }
}
