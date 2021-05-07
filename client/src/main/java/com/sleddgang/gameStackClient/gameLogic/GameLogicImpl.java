package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.config.GameValues;
import com.sleddgang.gameStackClient.util.enums.Option;

import java.util.Objects;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.playerTwoOption;
import static com.sleddgang.gameStackClient.config.GameValues.botOption;

@Component
@Log4j2
public class GameLogicImpl implements GameLogic {

  // == fields ==
  // public static Option playerOneOption;
  // public static Option botOption;
  private final Random rand = new Random();

  private final MessageGenerator messageGenerator;

  // == constructors ==
  public GameLogicImpl(MessageGeneratorImpl messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  @Override
  public boolean setPlayerOneOption(String option) {
    try {
      playerOneOption = Option.getOption(option);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("option");
      return false;
    }
    if (!playerOneOption.equals(Option.MAIN_MENU)) {
      messageGenerator.printSelectionMessage("You", playerOneOption.getLabel());
    }
    return true;
  }

  @Override
  public boolean setPlayerTwoOption(String option) {
    try {
      playerTwoOption = Option.getOption(option);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("option");
      return false;
    }
    if (!playerTwoOption.equals(Option.MAIN_MENU)) {
      messageGenerator.printSelectionMessage("You", playerTwoOption.getLabel());
    }
    return true;
  }

  @Override
  public void setBotOption() {
    botOption = Option.getOption(Integer.toString(rand.nextInt(3) + 1));
    messageGenerator.printSelectionMessage("Your opponent", botOption.getLabel());
  }

  @Override
  public void evaluateResults(Option playerOneOption, Option playerTwoOption) {
    log.debug("Player 1 Chose -- {}. Player 2 Chose -- {}", playerOneOption, playerTwoOption);
    String result;
    if (playerOneOption.equals(playerTwoOption)) {
      result = "You tied.";
    } else {
      String player2 = Objects.isNull(GameValues.botOption) ? "Player 2" : "Your opponent";
      result = didPlayerOneWin(playerOneOption, playerTwoOption) ? "Player 1 wins!" : format("%s wins!", player2);
    }
    messageGenerator.printResultMessage(result);
  }

  @Override
  public void refreshValues() {
    playerOneOption = null;
    playerTwoOption = null;
    botOption = null;
  }

  // == private methods ==
  private boolean didPlayerOneWin(Option playerOneOption, Option playerTwoOption) {
    return (playerOneOption.equals(Option.ROCK) && playerTwoOption.equals(Option.SCISSORS))
        || (playerOneOption.equals(Option.PAPER) && playerTwoOption.equals(Option.ROCK))
        || (playerOneOption.equals(Option.SCISSORS) && playerTwoOption.equals(Option.PAPER));
  }

}
