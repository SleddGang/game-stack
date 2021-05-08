package com.sleddgang.gameStackClient.game;

import java.util.Scanner;

public interface GameSetup {
    
    boolean setGameMode(String mode);

    void exitGame(Scanner keyboard);
}
