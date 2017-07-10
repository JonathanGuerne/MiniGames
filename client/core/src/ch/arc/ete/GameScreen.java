package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Client;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

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

    protected int gameId;

    protected  Skin skin;
    protected Table tableDisplay;
    protected Stage stage;

    boolean foundOpponent;
    boolean gameOver;
    boolean initGame;

    SpriteBatch batch;
    GlyphLayout layout;
    BitmapFont font;

    protected int gameLayoutWith = Gdx.graphics.getWidth() /10 * 8;
    protected int informationLayoutWith = Gdx.graphics.getWidth() / 10 * 2;


    public GameScreen(Client client, Player localPlayer) {
        this.client = client;
        this.localPlayer = localPlayer;
        this.foundOpponent = false;

        stage = new Stage();
        batch = new SpriteBatch();
        font = Util.createFont(48);
        layout = new GlyphLayout();
        final String text = "Waiting for an other player...";
        setCenterText(text);
        gameOver = false;
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

    public void setCenterText(String text){
        layout.setText(font, text, Color.BLACK, Gdx.graphics.getWidth(), Align.center, true);
    }

}
