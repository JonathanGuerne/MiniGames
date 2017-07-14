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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.GamePlayerLeavingPacket;
import packets.Packet;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * GameScreen.java   :  GameScreen is an abtract class implemented by all minis games and providing
 *                      functionality such as handling player leaving, displaying a waiting screen
 *                      before an opponent is found
 * ---------------------------------------------------------------------------------------------
 */

public abstract class GameScreen implements Screen, InputProcessor {

    protected Client client;

    protected HashMap<Integer, char[]> tabGame;
    protected int winnerId;

    protected int tabSize;

    protected int currentPlayerId;

    protected Player localPlayer;
    protected Player opponentPlayer;

    protected float w, h;

    protected int gameId;

    protected Skin skin;
    protected Table tableDisplay;
    protected Stage stage;
    protected Stage waitingStage;
    protected Label lblInfo;
    protected TextButton btnBack;

    protected boolean foundOpponent;
    protected boolean gameOver;
    protected boolean initGame;
    protected boolean gameLoaded;
    protected boolean initializationOver;
    protected boolean showMessage = false;

    protected SpriteBatch batch;
    private ShapeRenderer shapeRendererTextBox;
    protected GlyphLayout layout;
    protected BitmapFont font;

    protected int gameLayoutHeight = Gdx.graphics.getHeight() / 10 * 9;
    protected int informationLayoutHeight = Gdx.graphics.getHeight() - gameLayoutHeight;


    /**
     * constructor of the game screen
     * need a connection and a player
     * @param client connection to the server
     * @param localPlayer Player object
     */
    public GameScreen(final Client client, final Player localPlayer) {
        this.client = client;
        this.localPlayer = localPlayer;
        this.foundOpponent = false;
        this.initializationOver = false;

        //stage is use to display in game information and waiting stage is use to display a message while the player is waiting
        stage = new Stage();
        waitingStage = new Stage();

        batch = new SpriteBatch();
        shapeRendererTextBox = new ShapeRenderer();
        font = Util.createFont(48);
        layout = new GlyphLayout();

        final String text = "Attente d'un autre joueur...";
        setCenterText(text);

        skin = ApplicationSkin.getInstance().getSkin();

        btnBack = new TextButton("Retour", skin);

        waitingStage.addActor(btnBack);

        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //on "retour" button click
                playerLeft();
                ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, localPlayer));
            }
        });

        gameOver = false;
        gameLoaded = false;

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof GamePlayerLeavingPacket) {
                        //the server send us the info that our opponent as leave the game
                        GamePlayerLeavingPacket gplp = (GamePlayerLeavingPacket) o;
                        setCenterText("L'adversaire à quitté la partie...");
                        gameOver = true;
                        winnerId = -10;
                    }

                }
            }
        });

        Gdx.input.setInputProcessor(waitingStage);
    }

    @Override
    public void render(float delta) {
        if (foundOpponent) {
            if (!initializationOver) {
                initializationOver = true;

                /*
                *   The inputmulitplexer is use so that we can both use element of the graphical interface
                *   such as a button in this case and get an event when a click on the screen append.
                *   We need this because otherwise we couldn't handle both ingame input such as touch the screen to select a case
                *   and using button like for example the "retour" button
                */
                InputMultiplexer multiplexer = new InputMultiplexer();
                multiplexer.addProcessor(this);
                multiplexer.addProcessor(stage);
                Gdx.input.setInputProcessor(multiplexer);
                initInformationTable();


            }
            update();
            display();

            if (gameLoaded) {
                stage.act();
                stage.draw();
            }

            if (gameOver) {
                float x = 0;
                float y = Gdx.graphics.getHeight() / 2 + layout.height / 2;

                batch.begin();
                font.draw(batch, layout, x, y);
                batch.end();
            }
        } else {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            ApplicationSkin.getInstance().showBackground();

            float x = 0;
            float y = Gdx.graphics.getHeight() / 2 + layout.height / 2;

            batch.begin();
            font.draw(batch, layout, x, y);
            batch.end();

            waitingStage.act();
            waitingStage.draw();
        }
    }

    /**
     * abstract function for displaying content
     */
    public abstract void display();

    /**
     * abstract function for updating content
     */
    public abstract void update();

    /**
     * we call this method when a player is about to left the game. it send a message to the server
     * letting him know that the game is over
     */
    protected void playerLeft() {
        GamePlayerLeavingPacket gplp = new GamePlayerLeavingPacket();
        gplp.playerid = localPlayer.getId();
        gplp.playerName = localPlayer.getPseudo();
        client.sendTCP(gplp);
    }

    /**
     * abstract method that allow a mini game to add element into the part of the screen displaying
     * in game information
     */
    protected abstract void setGameMenu();

    /**
     * simplification call for changing the text display in the center of the screen
     * @param text text to display
     */
    protected void setCenterText(String text) {
        layout.setText(font, text, Color.BLACK, Gdx.graphics.getWidth(), Align.center, true);
    }

    /**
     * generate the part of the screen displayin the information about the game
     * including the "retour" button used to go back to the main menu
     */
    protected void initInformationTable() {
        tableDisplay = new Table();
        tableDisplay.setPosition(0, gameLayoutHeight);
        tableDisplay.setWidth(Gdx.graphics.getWidth());
        tableDisplay.setHeight(informationLayoutHeight);
        tableDisplay.center();

        stage.addActor(tableDisplay);

        lblInfo = new Label(localPlayer.getPseudo() + " VS " + opponentPlayer.getPseudo(), skin);
        tableDisplay.add(lblInfo);
        tableDisplay.row();

        setGameMenu();

        tableDisplay.add(btnBack).width(300);


        gameLoaded = true;
    }

    /**
     * use to display in the center of the screen witch player turn it is
     * @param opponentName require the name of the opponent player
     */
    protected void displayCurrentPlayer(String opponentName) {
        String text = (currentPlayerId == localPlayer.getId()) ? "C'est votre tour." : "C'est le tour de " + opponentName;
        setCenterText(text);
        showMessage = true;
    }
}
