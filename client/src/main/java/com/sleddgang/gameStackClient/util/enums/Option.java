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
  MAIN_MENU("Main Menu", 4);

  @Getter
  private final String label;

  private final int value;

  Option(String label, int value) {
    this.label = label;
    this.value = value;
  }

  public static Option getOption(String option) {
    log.debug("Player's option -- {}", option);
    return EnumSet.allOf(Option.class).stream().filter(
        o -> o.label.equalsIgnoreCase(option) || Integer.toString(o.value).equalsIgnoreCase(option))
        .findFirst().orElseThrow(
            () -> new IllegalArgumentException(format("Invalid option '%s' entered.", option)));
  }
}
