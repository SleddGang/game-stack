package com.sleddgang.gameStackClient.gameLogic;

import java.util.Scanner;

public interface GameSetup {
    
    boolean setGameMode(String mode);

    void exitGame(Scanner keyboard);
}
