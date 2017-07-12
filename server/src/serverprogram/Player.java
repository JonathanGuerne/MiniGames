package serverprogram;

/**
 * Created by jonathan.guerne on 11.07.2017.
 */

public class Player {
    private int idPlayer;
    private String namePlayer;
    private boolean isPlaying = false;
    private int currentGameId;

    public Player(int idPlayer, String namePlayer) {
        this.idPlayer = idPlayer;
        this.namePlayer = namePlayer;
    }

    public int getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGameId(int currentGameId) {
        this.currentGameId = currentGameId;
    }

    public int getIdPlayer() {
        return idPlayer;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setIdPlayer(int idPlayer) {
        this.idPlayer = idPlayer;
    }

    public String getNamePlayer() {
        return namePlayer;
    }

    public void setNamePlayer(String namePlayer) {
        this.namePlayer = namePlayer;
    }
}
