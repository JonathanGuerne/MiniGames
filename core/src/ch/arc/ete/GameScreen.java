package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Client;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public abstract class GameScreen implements Screen {

    protected Client client;

    protected HashMap<Integer, char[]> tabGame;
    protected int winnerId;

    protected int tabSize;

    protected int currentPlayerID;

    protected Player localPlayer;
    protected Player opponentPlayer;

    boolean foundOpponent;

    SpriteBatch batch;
    GlyphLayout layout;
    BitmapFont font;


    public GameScreen(Client client) {
        this.client = client;
        this.localPlayer = new Player(client.getID(),"Bob");
        this.foundOpponent = false;

        batch = new SpriteBatch();
        font = Util.createFont(48);
        layout = new GlyphLayout();
        final String text = "Waiting for an other player...";
        layout.setText(font, text, Color.BLACK, Gdx.graphics.getWidth(), Align.center, true);
    }

    @Override
    public void render(float delta) {
        if (foundOpponent) {
            update();
            display();
        } else {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            float x = 0;
            float y = Gdx.graphics.getHeight()/2 + layout.height/2;

            batch.begin();
            font.draw(batch,layout,x,y);
            batch.end();
        }
    }

    public abstract void display();

    public abstract void update();

}
