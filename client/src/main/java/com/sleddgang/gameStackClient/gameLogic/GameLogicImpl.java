package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.util.enums.Option;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class GameLogicImpl implements GameLogic {

  // == fields ==
  public static Option playerOption;
  public static Option botOption;
  private final Random rand = new Random();

  private final MessageGenerator messageGenerator;

  // == constructors ==
  public GameLogicImpl(MessageGeneratorImpl messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  @Override
  public boolean setPlayerOption(String option) {
    try {
      playerOption = Option.getOption(option);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("option");
      return false;
    }
    if (!playerOption.equals(Option.MAIN_MENU)) {
      messageGenerator.printSelectionMessage("You", playerOption.getLabel());
    }
    return true;
  }

  @Override
  public void setBotOption() {
    botOption = Option.getOption(Integer.toString(rand.nextInt(3) + 1));
    messageGenerator.printSelectionMessage("Your opponent", botOption.getLabel());
  }

  @Override
  public void evaluateResults(Option playerOption, Option botOption) {
    log.debug("Player Chose -- {}. Bot Chose -- {}", playerOption, botOption);
    String result;
    if (playerOption.equals(botOption)) {
      result = "tied.";
    } else {
      result = didPlayerWin(playerOption, botOption) ? "won!" : "lost :(";
    }
    messageGenerator.printResultMessage(result);
  }

  // == private methods ==
  private boolean didPlayerWin(Option playerOption, Option botOption) {
    return (playerOption.equals(Option.ROCK) && botOption.equals(Option.SCISSORS))
        || (playerOption.equals(Option.PAPER) && botOption.equals(Option.ROCK))
        || (playerOption.equals(Option.SCISSORS) && botOption.equals(Option.PAPER));
  }
}
