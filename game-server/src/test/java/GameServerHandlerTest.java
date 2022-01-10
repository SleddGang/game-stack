import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleddgang.gameStackGameServer.GameServerApplication;
import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.schemas.Error;
import com.sleddgang.gameStackGameServer.schemas.*;
import com.sleddgang.gameStackGameServer.schemas.events.ResultEvent;
import com.sleddgang.gameStackGameServer.schemas.events.ServerStatusEvent;
import com.sleddgang.gameStackGameServer.schemas.methods.*;
import com.sleddgang.gameStackGameServer.schemas.replies.*;
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

import java.io.IOException;
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
    private ArrayBlockingQueue<String> clientOneBlockingQueue;
    private ArrayBlockingQueue<String> clientTwoBlockingQueue;
    private ArrayBlockingQueue<String> matchBlockingQueue;
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
        clientOneBlockingQueue = new ArrayBlockingQueue(10);    //Used to send messages out of the handler.
        clientTwoBlockingQueue = new ArrayBlockingQueue(10);    //Used to send messages out of the handler.
        matchBlockingQueue = new ArrayBlockingQueue(10);    //Used to send messages out of the handler.

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

    @Test
    public void verifyAddClient() throws Exception {
        String client = "client";

        long reqid = 1;

        TextWebSocketHandler matchHandler = createHandler(message -> {
            System.out.println("Match Handler: " + message);
            matchBlockingQueue.add(message);
        });

        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        TextMessage matchMessage = new TextMessage(objectMapper.writeValueAsBytes(new AddClientMethod(client, reqid)));
        TextMessage repeatMatchMessage = new TextMessage(objectMapper.writeValueAsBytes(new AddClientMethod(client, reqid + 1)));

        matchSession.sendMessage(matchMessage);

        AbstractGameMessage reply = objectMapper.readValue(matchBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkAddClientReply(reply, reqid, client);

        matchSession.sendMessage(repeatMatchMessage);

        reply = objectMapper.readValue(matchBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkError(reply, reqid + 1, Error.DUPLICATE_CLIENT);
    }

    //Verifies that when one client disconnects the other client will be disconnected.
    @Test
    public void verifyShutdown() throws Exception {
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        createMatch(new String[]{client1, client2}, new WebSocketSession[]{matchSession, gameOneSession, gameTwoSession}, reqid);

        reqid++;

        //Close one client and ensure the second client gets closed as well.
        gameOneSession.close();
        TimeUnit.MILLISECONDS.sleep(1000);
        Assert.isTrue(!gameOneSession.isOpen(), "GameOneSession should not be open");
        Assert.isTrue(!gameTwoSession.isOpen(), "GameTwoSession should not be open");

//        //Recreate the same match to ensure it is shut down.
//        gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
//        gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
//
//        createMatch(new String[]{client1, client2}, new WebSocketSession[]{matchSession, gameOneSession, gameTwoSession}, reqid);
//
//        reqid++;
//
//        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client1, reqid))));
//        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(client2, reqid))));
//
//        gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
//        gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
//        checkJoinReply(gameOneResponse, reqid);
//        checkJoinReply(gameTwoResponse, reqid);
//
//        gameOneSession.close();
//        gameTwoSession.close();
//        matchSession.close();
    }

    //Verifies the game logic by having two clients play joining and playing and check to make sure they win/lose correctly.
    @Test
    public void verifyGame() throws Exception {
        String client1 = "gameClient1";
        String client2 = "gameClient2";

        long reqid = 1;

        WebSocketSession gameOneSession = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession gameTwoSession = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);

        createMatch(new String[]{client1, client2}, new WebSocketSession[]{matchSession, gameOneSession, gameTwoSession}, reqid);

        reqid++;

        //Client one plays rock and client two plays paper so client two should win and client one should lose.
        gameOneSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new MoveMethod(Option.ROCK, reqid))));
        gameTwoSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new MoveMethod(Option.PAPER, reqid))));

        AbstractGameMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        AbstractGameMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkResult(gameOneResponse, reqid, Result.LOSS);
        checkResult(gameTwoResponse, reqid, Result.WIN);

        gameOneSession.close();
        gameTwoSession.close();
        matchSession.close();
    }

    @Test
    public void verifyStatus() throws Exception {
        int runs = 5;
        WebSocketSession[] gameOneSessions = new WebSocketSession[runs];
        WebSocketSession[] gameTwoSessions = new WebSocketSession[runs];
        WebSocketSession matchSession = matchClientSocket.doHandshake(matchHandler, null, matchUri).get(1, TimeUnit.SECONDS);
        for (int i = 0; i < runs; i++) {
            String client1 = "gameClient1" + i;
            String client2 = "gameClient2" + i;

            //If i is less than the max number of matches then slotsLeft should be the max number of matches - i.
            //Otherwise slotsLeft should be 0 as we can't have more than the max number of matches.
            int slotsLeft = i < 2 ? 1 - i : 0;

            //Only when we have filled up all the matches should the client queue fill up.
            int clientQueue = i > 1 ? (i - 1) * 2 : 0;

            gameOneSessions[i] = clientOneSocket.doHandshake(clientOneHandler, null, gameUri).get(1, TimeUnit.SECONDS);
            gameTwoSessions[i] = clientTwoSocket.doHandshake(clientTwoHandler, null, gameUri).get(1, TimeUnit.SECONDS);

            createMatch(new String[]{client1, client2}, new WebSocketSession[]{matchSession, gameOneSessions[i], gameTwoSessions[i]}, i);

            matchSession.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new GetStatusMethod(i))));

            AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
            checkStatus(matchResponse, slotsLeft, 2, clientQueue);
        }

        for (int i = 0; i < runs; i++) {
            gameOneSessions[i].close();
            gameTwoSessions[i].close();
        }
    }

    private void createMatch(String[] clients, WebSocketSession[] sessions, long reqid) throws InterruptedException, IOException {
        //Add the first client to the server.
        sessions[0].sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new AddClientMethod(clients[0], reqid))));

        //Verify that the server responds with an add client reply and a status event.
        AbstractGameMessage matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkAddClientReply(matchResponse, reqid, clients[0]);

        //Add the second client to the server.
        sessions[0].sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new AddClientMethod(clients[1], reqid + 100))));

        //Verify that the server responds with an add client reply and a status event.
        matchResponse = objectMapper.readValue(matchBlockingQueue.poll(5, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkAddClientReply(matchResponse, reqid + 100, clients[1]);

        //Both client's join the match.
        sessions[1].sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(clients[0], reqid))));
        sessions[2].sendMessage(new TextMessage(objectMapper.writeValueAsBytes(new JoinMethod(clients[1], reqid))));

        AbstractGameMessage gameOneResponse = objectMapper.readValue(clientOneBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        AbstractGameMessage gameTwoResponse = objectMapper.readValue(clientTwoBlockingQueue.poll(1, TimeUnit.SECONDS), AbstractGameMessage.class);
        checkJoinReply(gameOneResponse, reqid);
        checkJoinReply(gameTwoResponse, reqid);
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
        Assert.isTrue(type.isInstance(response), "The response event must be a " + type + ". Is type" + response.getClass());
        AbstractGameReply reply = (AbstractGameReply) response;
        Assert.isTrue(reply.id == reqid, "The response reqid must be the same as the request reqid.");
    }

    //Calls checkReply and verifies that the match uuid is correct.
    private void checkJoinReply(AbstractGameMessage response, long reqid) {
        checkReply(response, reqid, JoinReply.class);
        JoinReply joinReply = (JoinReply) response;
    }

    //Calls checkReply and verifies that the client uuid is correct.
    private void checkAddClientReply(AbstractGameMessage response, long reqid, String clientUuid) {
        checkReply(response, reqid, AddClientReply.class);
        AddClientReply addClientReply = (AddClientReply) response;
        Assert.isTrue(addClientReply.clientUuid.equals(clientUuid),
                "AddClientReply clientUuid should be the same as the requested clientUuid");
    }

    //Checks that the response is a ServerStatusReply and verifies that the slots left is correct.
    private void checkStatus(AbstractGameMessage response, int gameSlotsLeft, int maxGameSlots, long clientQueue) {
        Assert.isTrue(response instanceof ServerStatusReply, "The response must be of type ServerStatusReply");
        ServerStatusReply serverStatusEvent = (ServerStatusReply) response;
        Assert.isTrue(serverStatusEvent.gameSlotsLeft == gameSlotsLeft, "Slots left must be " + gameSlotsLeft + ".");
        Assert.isTrue(serverStatusEvent.maxGameSlots == maxGameSlots, "Max slots must be " + maxGameSlots + ".");
        Assert.isTrue(serverStatusEvent.clientQueue == clientQueue, "Client queue length must be " + clientQueue + ".");
    }

    //Checks that the response is a ResultEvent and verifies that the result is correct.
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
