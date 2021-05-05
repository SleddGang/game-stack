package com.sleddgang.gameStackClient;

import com.sleddgang.gameStackClient.gameLogic.GameSetup;
import com.sleddgang.gameStackClient.gameLogic.GameSetupImpl;
import com.sleddgang.gameStackClient.gameLogic.gameModeLogic.SinglePlayerGame;
import java.util.Scanner;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RockPaperScissorsClient {

  // == fields ==
  private final GameSetup gameSetup;
  private final SinglePlayerGame singlePlayerGame;
  private final Scanner keyboard = new Scanner(System.in);

  private boolean gameContinue = true;

  // == constructors ==
  public RockPaperScissorsClient(GameSetupImpl gameSetup, SinglePlayerGame singlePlayerGame) {
    this.gameSetup = gameSetup;
    this.singlePlayerGame = singlePlayerGame;
  }

  // == init ==
  @PostConstruct
  public void init() {
    log.debug("Initializing game. Entering while loop");
  }

  @EventListener(ContextRefreshedEvent.class)
  public void start() {

    // While loop allows player to continue playing until they enter "Quit"
    while (gameContinue) {

      log.info("Welcome to Rock Paper Scissors!" +
          "\n===============================" +
          "\nPlease select a game mode:" +
          "\n(1) Single Player" +
          "\n(2) Online Multiplayer" +
          "\n(3) Local Multiplayer" +
          "\n(4) Quit" +
          "\n===============================\n");

      if (!gameSetup.setGameMode(keyboard.nextLine())) {
        continue;
      }

      switch (GameSetupImpl.gameMode) {
        case SINGLE_PLAYER:
          singlePlayerGame.playSinglePlayerGame(keyboard);
          break;
        case ONLINE_MULTIPLAYER:
          singlePlayerGame.playSinglePlayerGame(keyboard);
          break;
        case LOCAL_MULTIPLAYER:
          singlePlayerGame.playSinglePlayerGame(keyboard);
          break;
        case QUIT:
          gameContinue = false;
          break;
      }

    }

    // Shuts down game after player quits while loop
    keyboard.close();
    log.info("Thank you for playing!\n");
    System.exit(0);
  }

}
