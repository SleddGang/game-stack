package com.sleddgang.gameStackClient;

import com.sleddgang.gameStackClient.gameLogic.GameSetup;
import com.sleddgang.gameStackClient.gameLogic.GameSetupImpl;
import com.sleddgang.gameStackClient.gameLogic.MessageGenerator;
import com.sleddgang.gameStackClient.gameLogic.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.gameLogic.gameModeLogic.LocalMultiplayerGame;
import com.sleddgang.gameStackClient.gameLogic.gameModeLogic.SinglePlayerGame;
import java.util.Scanner;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.sleddgang.gameStackClient.config.GameValues.gameMode;

@Component
@Log4j2
public class RockPaperScissorsClient {

  // == fields ==
  private final GameSetup gameSetup;
  private final SinglePlayerGame singlePlayerGame;
  private final LocalMultiplayerGame localMultiplayerGame;
  private final MessageGenerator messageGenerator;
  private final Scanner keyboard = new Scanner(System.in);

  private boolean gameContinue = true;

  // == constructors ==
  public RockPaperScissorsClient(GameSetupImpl gameSetup, SinglePlayerGame singlePlayerGame,
      LocalMultiplayerGame localMultiplayerGame,
      MessageGeneratorImpl messageGenerator) {
    this.gameSetup = gameSetup;
    this.singlePlayerGame = singlePlayerGame;
    this.localMultiplayerGame = localMultiplayerGame;
    this.messageGenerator = messageGenerator;
  }

  // == init ==
  @PostConstruct
  public void init() {
    log.debug("Initializing game. Entering while loop");
    messageGenerator.printWelcomeMessage();
  }

  // == Event Listener ==
  @EventListener(ContextRefreshedEvent.class)
  public void start() {

    // While loop allows player to continue playing until they enter "Quit"
    while (gameContinue) {

      // Prints the main menu where players can select their game mode
      messageGenerator.printMainMenu();

      // Sets the game mode that the player selects.
      // If the player enters an invalid game mode, it resets the loop
      if (!gameSetup.setGameMode(keyboard.nextLine())) {
        continue;
      }

      // Routes the player to the correct game mode code
      switch (gameMode) {
        case SINGLE_PLAYER:
          singlePlayerGame.playSinglePlayerGame(keyboard);
          break;
        case ONLINE_MULTIPLAYER:
          // TODO: Implement online multiplayer code
          break;
        case LOCAL_MULTIPLAYER:
          localMultiplayerGame.playLocalMultiplayerGame(keyboard);
          break;
        case QUIT:
          gameContinue = false;
          break;
      }

    }

    // Shuts down game after player quits while loop
    gameSetup.exitGame(keyboard);
  }

}
