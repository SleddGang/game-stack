package com.sleddgang.gameStackClient;

import com.sleddgang.gameStackClient.annotations.GameMenu;
import com.sleddgang.gameStackClient.gameLogic.GameLogic;
import com.sleddgang.gameStackClient.gameLogic.GameLogicImpl;
import com.sleddgang.gameStackClient.util.enums.GameMode;
import com.sleddgang.gameStackClient.util.enums.Option;
import java.util.Random;
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
  private final GameLogic gameLogic;
  private final String gameMenu;
  private final GameMode gameMode;
  private final Random rand = new Random();
  private final Scanner keyboard = new Scanner(System.in);

  // == constructors ==
  public RockPaperScissorsClient(GameLogicImpl gameLogic, @GameMenu String gameMenu) {
    this.gameLogic = gameLogic;
    this.gameMenu = gameMenu;
    log.info("Welcome to Rock Paper Scissors!" +
             "\n===============================" +
             "\nPlease select a game mode:" +
             "\n(1) Single Player" +
             "\n(2) Online Multiplayer" +
             "\n(3) Local Multiplayer" +
             "\n===============================\n");
    this.gameMode = GameMode.getGameMode(keyboard.next());
  }

  // == init ==
  @PostConstruct
  public void init() {
    log.debug("Initializing game. Entering while loop");
  }

  @EventListener(ContextRefreshedEvent.class)
  public void start() {

    System.out.println(gameMode);

    // While loop allows player to continue playing until they enter "Quit"
    while (true) {

      // Displays welcome message
      gameLogic.printGameMenu(gameMenu);

      // Grabs user's option choice
      Option playerOption;
      try {
        playerOption = Option.getOption(keyboard.next());
      } catch (IllegalArgumentException e) {
        log.info("Please enter a valid option from the list");
        log.debug(e);
        continue;
      }

      // Breaks out of while loop if player enters "Quit"
      if (playerOption.equals(Option.QUIT)) {
        log.debug("Exiting game");
        break;
      }

      // Sets the bot's option
      Option botOption = Option.getOption(rand.nextInt(3) + 1);

      // Evaluates and prints the game's result
      gameLogic.evaluateResults(playerOption, botOption);
    }

    // Shuts down game after player quits while loop
    keyboard.close();
    log.info("Thank you for playing!\n");
    System.exit(0);
  }

}
