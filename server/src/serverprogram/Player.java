package serverprogram;

/**
 * Created by jonathan.guerne on 11.07.2017.
 */

public class Player {
    private int idPlayer;
    private String namePlayer;

    public Player(int idPlayer, String namePlayer) {
        this.idPlayer = idPlayer;
        this.namePlayer = namePlayer;
    }

    public int getIdPlayer() {
        return idPlayer;
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
