package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.esotericsoftware.kryonet.Client;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public abstract class GameScreen implements Screen,InputProcessor {

    protected Client client;

    protected HashMap<Integer, char[]> tabGame;
    protected int winnerId;

    protected int tabSize;

    protected int currentPlayerId;

    protected Player localPlayer;
    protected Player opponentPlayer;

    protected int gameId;

    protected  Skin skin;
    protected Table tableDisplay;
    protected Stage stage;
    protected Stage waitingStage;
    protected Label currentPlayerPseudo;
    protected Label opponentPlayerPseudo;
    protected Label playerTurn;
    protected Label vsLabel;
    protected TextButton btnBack;

    boolean foundOpponent;
    boolean gameOver;
    boolean initGame;
    boolean gameLoaded;
    boolean initializationOver;

    SpriteBatch batch;
    GlyphLayout layout;
    BitmapFont font;



    protected int gameLayoutWith = Gdx.graphics.getWidth() /10 * 8;
    protected int informationLayoutWith = Gdx.graphics.getWidth() / 10 * 2;



    public GameScreen(final Client client, final Player localPlayer) {
        this.client = client;
        this.localPlayer = localPlayer;
        this.foundOpponent = false;
        this.initializationOver = false;

        stage = new Stage();
        waitingStage = new Stage();
        batch = new SpriteBatch();
        font = Util.createFont(48);
        layout = new GlyphLayout();
        final String text = "Attente d'un autre joueur...";
        setCenterText(text);

        skin = ApplicationSkin.getInstance().getSkin();

        btnBack = new TextButton("Retour",skin);

        waitingStage.addActor(btnBack);

        btnBack.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playerLeft();
                ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client,localPlayer));
            }
        });

        gameOver = false;
        gameLoaded = false;

        Gdx.input.setInputProcessor(waitingStage);
    }

    @Override
    public void render(float delta) {
        if (foundOpponent) {
            if(!initializationOver){
                initializationOver = true;

                InputMultiplexer multiplexer = new InputMultiplexer();
                multiplexer.addProcessor(this);
                multiplexer.addProcessor(stage);
                Gdx.input.setInputProcessor(multiplexer);
                initInformationTable();
            }
            update();
            display();

            if(gameOver){
                float x = 0;
                float y = Gdx.graphics.getHeight()/2 + layout.height/2;

                batch.begin();
                font.draw(batch,layout,x,y);
                batch.end();
            }
        } else {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            ApplicationSkin.getInstance().showBackground();

            float x = 0;
            float y = Gdx.graphics.getHeight()/2 + layout.height/2;

            batch.begin();
            font.draw(batch,layout,x,y);
            batch.end();

            waitingStage.act();
            waitingStage.draw();
        }
    }

    public abstract void display();

    public abstract void update();

    public abstract void playerLeft();

    public void setCenterText(String text){
        layout.setText(font, text, Color.BLACK, Gdx.graphics.getWidth(), Align.center, true);
    }

    protected void initInformationTable()
    {
        tableDisplay = new Table();
        tableDisplay.setPosition(gameLayoutWith, 0);
        tableDisplay.setWidth(informationLayoutWith);
        tableDisplay.setHeight(Gdx.graphics.getHeight());
        tableDisplay.center();

        stage.addActor(tableDisplay);

        currentPlayerPseudo = new Label(localPlayer.getPseudo(), skin);
        currentPlayerPseudo.setColor(0, 255, 0, 255);
        tableDisplay.add(currentPlayerPseudo);
        tableDisplay.row();

        vsLabel = new Label("VS", skin);
        vsLabel.setColor(0, 255, 0, 255);
        tableDisplay.add(vsLabel);
        tableDisplay.row();

        opponentPlayerPseudo = new Label(opponentPlayer.getPseudo(), skin);
        opponentPlayerPseudo.setColor(0, 255, 0, 255);
        tableDisplay.add(opponentPlayerPseudo);
        tableDisplay.row();

        playerTurn = new Label("", skin);
        playerTurn.setColor(0, 255, 0, 255);
        tableDisplay.add(playerTurn);
        tableDisplay.row();

        tableDisplay.add(btnBack);


        gameLoaded = true;
    }
}
