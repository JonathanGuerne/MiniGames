package serverprogram;

/**
 * Created by jonathan.guerne on 21.06.2017.
 */

public class Game {
    private int id;

    private int idPlayer1;
    private int idPlayer2;

    private char charPlayer1;
    private char charPlayer2;

    private GameType type;

    /**
     * create a new Game class use by the server to store information
     * @param type type of game (morpion,battleship,...)
     * @param idPlayer1
     * @param idPlayer2
     * @param id id of the game UNIQUE
     */
    public Game(GameType type, int idPlayer1, int idPlayer2, int id) {
        this.type = type;
        this.idPlayer2 = idPlayer2;
        this.idPlayer1 = idPlayer1;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPlayer1() {
        return idPlayer1;
    }

    public void setIdPlayer1(int idPlayer1) {
        this.idPlayer1 = idPlayer1;
    }

    public int getIdPlayer2() {
        return idPlayer2;
    }

    public void setIdPlayer2(int idPlayer2) {
        this.idPlayer2 = idPlayer2;
    }

    public char getCharPlayer1() {
        return charPlayer1;
    }

    public void setCharPlayer1(char charPlayer1) {
        this.charPlayer1 = charPlayer1;
    }

    public char getCharPlayer2() {
        return charPlayer2;
    }

    public void setCharPlayer2(char charPlayer2) {
        this.charPlayer2 = charPlayer2;
    }

    public GameType getType() {
        return type;
    }

    public void setType(GameType type) {
        this.type = type;
    }
}
