package ch.arc.ete;

/**
 * Created by jonathan.guerne on 31.05.2017.
 */

public class Player {
    private int id;
    private String pseudo;

    public Player(int id, String pseudo) {
        this.id = id;
        this.pseudo = pseudo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}
