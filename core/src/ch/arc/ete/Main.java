package ch.arc.ete;

import com.badlogic.gdx.Game;

/**
 * Created by jonathan.guerne on 27.04.2017.
 */

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new LoginScreen());
    }
}
