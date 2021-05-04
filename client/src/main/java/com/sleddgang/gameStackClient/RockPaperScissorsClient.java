package com.sleddgang.gameStackClient;

import com.sleddgang.gameStackClient.annotations.GameMenu;
import com.sleddgang.gameStackClient.gameLogic.GameEvaluation;
import com.sleddgang.gameStackClient.gameLogic.GameEvaluationImpl;
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
  private final GameEvaluation gameEvaluation;
  private final String gameMenu;
  private final Random rand = new Random();
  private final Scanner keyboard = new Scanner(System.in);

  // == constructors ==
  public RockPaperScissorsClient(GameEvaluationImpl gameEvaluation, @GameMenu String gameMenu) {
    this.gameEvaluation = gameEvaluation;
    this.gameMenu = gameMenu;
  }

  // == init ==
  @PostConstruct
  public void init() {
    log.debug("Initializing game. Entering while loop");
    log.info("Welcome to Rock Paper Scissors!");
  }

  @EventListener(ContextRefreshedEvent.class)
  public void start() {

    // While loop allows player to continue playing until they enter "Quit"
    while (true) {

      // Displays welcome message
      gameEvaluation.printGameMenu(gameMenu);

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
      gameEvaluation.evaluateResults(playerOption, botOption);
    }

    // Shuts down game after player quits while loop
    keyboard.close();
    log.info("Thank you for playing!\n");
    System.exit(0);
  }

}
