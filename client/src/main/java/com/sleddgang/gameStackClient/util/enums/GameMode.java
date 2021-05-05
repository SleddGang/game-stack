package com.sleddgang.gameStackClient.util.enums;

import static java.lang.String.format;

import java.util.EnumSet;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public enum GameMode {
  SINGLE_PLAYER("Single Player", 1),
  ONLINE_MULTIPLAYER("Online Multiplayer", 2),
  LOCAL_MULTIPLAYER("Local Multiplayer", 3),
  QUIT("Quit", 4);

  private final String label;
  private final int value;

  GameMode(String label, int value) {
    this.label = label;
    this.value = value;
  }

  public static GameMode getGameMode(String mode) {
    log.debug("Player's game mode -- {}", mode);
    return EnumSet.allOf(GameMode.class).stream().filter(o -> o.label.equalsIgnoreCase(mode) || Integer.toString(o.value).equalsIgnoreCase(mode)).
        findFirst().orElseThrow(() -> new IllegalArgumentException(format("Invalid mode '%s' entered.", mode)));
  }
}
