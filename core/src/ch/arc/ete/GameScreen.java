package ch.arc.ete;

import com.badlogic.gdx.Screen;
import com.esotericsoftware.kryonet.Client;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public abstract class GameScreen implements Screen {

    Client client;

    public GameScreen(Client client) {
        this.client = client;
    }
}
