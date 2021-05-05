package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.util.enums.GameMode;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class GameSetupImpl implements GameSetup {
    
public static GameMode gameMode;

public boolean setGameMode(String mode) {
    try {
        gameMode = GameMode.getGameMode(mode);
    } catch (IllegalArgumentException e) {
        log.debug(e);
        log.info("Please enter a valid game mode.");
        return false;
    }
    return true;
}

}
