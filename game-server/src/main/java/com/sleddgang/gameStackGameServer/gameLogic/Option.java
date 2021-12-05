package com.sleddgang.gameStackGameServer.gameLogic;

/**
 * Used to represent a move.
 *
 * @author Benjamin
 */
public enum Option {
    ROCK("Rock", 1),
    PAPER("Paper", 2),
    SCISSORS("Scissors", 3);

    private final String label;
    private final int value;

    Option(String label, int value) {
        this.label = label;
        this.value = value;
    }
}
