package com.sleddgang.gameStackClient.gameLogic;

import static com.sleddgang.gameStackClient.util.TextUtil.prettifyText;

import com.sleddgang.gameStackClient.util.enums.Option;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class GameEvaluationImpl implements GameEvaluation {

  @Override
  public void printGameMenu(String gameMenu) {
    log.info("\n{}\n", prettifyText(gameMenu));
  }

  @Override
  public boolean didPlayerWin(Option playerOption, Option botOption) {
    return (playerOption.equals(Option.ROCK) && botOption.equals(Option.SCISSORS))
        || (playerOption.equals(Option.PAPER) && botOption.equals(Option.ROCK))
        || (playerOption.equals(Option.SCISSORS) && botOption.equals(Option.PAPER));
  }

  @Override
  public void evaluateResults(Option playerOption, Option botOption) {
    log.debug("Player Chose -- {}. Bot Chose -- {}", playerOption, botOption);
    StringBuilder result = new StringBuilder("You ");
    if (playerOption.equals(botOption)) {
      result.append("tied.");
    } else {
      result.append(didPlayerWin(playerOption, botOption) ? "won!" : "lost :(");
    }
    log.info(result);
  }
}
