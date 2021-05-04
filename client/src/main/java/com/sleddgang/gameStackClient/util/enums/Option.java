package com.sleddgang.gameStackClient.util.enums;

import static java.lang.String.format;

import java.util.EnumSet;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public enum Option {
  ROCK("Rock", 1),
  PAPER("Paper", 2),
  SCISSORS("Scissors", 3),
  QUIT("Quit", 4);

  @Getter
  public final String label;

  private final int number;

  Option(String label, int number) {
    this.label = label;
    this.number = number;
  }

  public static Option getOption(String option) {
    log.debug("Player's option -- {}", option);
    Option choice = EnumSet.allOf(Option.class).stream().filter(o -> o.label.equalsIgnoreCase(option)).
        findFirst().orElseThrow(() -> new IllegalArgumentException(format("Invalid option '%s' entered.", option)));
    if (!choice.equals(QUIT)) {
      log.info("You used {}", choice.label);
    }
    return choice;
  }

  public static Option getOption(int num) {
    log.debug("bot's random number -- {}", num);
    Option choice = EnumSet.allOf(Option.class).stream().filter(o -> o.number == num).
        findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid option entered."));
    log.info("Your opponent used {}", choice.label);
    return choice;
  }

}
