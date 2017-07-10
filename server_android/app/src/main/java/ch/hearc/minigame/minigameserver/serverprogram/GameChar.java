package ch.hearc.minigame.minigameserver.serverprogram;

/**
 * Created by jonathan.guerne on 02.07.2017.
 */
public class GameChar {
    private char infoChar;

    public GameChar(char infoChar) {
        this.infoChar = infoChar;
    }

    public GameChar() {
    }

    public char getInfoChar() {
        return infoChar;
    }

    public void setInfoChar(char infoChar) {
        this.infoChar = infoChar;
    }
}
