package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * MainMenu.java   :  MainMenu is the class use to select witch game will be played
 * ---------------------------------------------------------------------------------------------
 */

public class MainMenu implements Screen {
    private SpriteBatch batch;

    private Client client;
    private Player localPlayer;

    static int tcp = 23900, udp = 23901;

    private Table tableMenu;

    private Stage stage;

    private TextButton btnMoripon;
    private TextButton btnBattleShip;
    private TextButton btnBack;
    private TextButton btnQuit;


    private boolean lostConnection;

    /**
     * maimmenu constructor using a connection to the server and the user pseudo
     * @param client connecion
     * @param pseudo pseudo
     */
    public MainMenu(final Client client, String pseudo) {
        this.client = client;
        this.localPlayer = new Player(this.client.getID(), pseudo);

    }

    /**
     * mainmenu constructor using a connection to the server and a already create Player object
     * @param client connection
     * @param localPlayer playerObject
     */
    public MainMenu(final Client client, Player localPlayer)
    {
        this.client = client;
        this.localPlayer = localPlayer;
    }


    @Override
    public void show() {

        client.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                client.close();
                lostConnection = true;
            }

        });


        batch = new SpriteBatch();
        tableMenu = new Table();
        stage = new Stage();

        tableMenu.setPosition(0,0);
        tableMenu.setHeight(Gdx.graphics.getHeight());
        tableMenu.setWidth(Gdx.graphics.getWidth());

        tableMenu.center();

        Skin skin = ApplicationSkin.getInstance().getSkin();

        btnMoripon = new TextButton("Jouer au morpion",skin);
        btnBattleShip = new TextButton("Jouer a la bataille navale", skin);
        btnBack = new TextButton("Retour",skin);
        btnQuit = new TextButton("Quitter",skin);

        btnMoripon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new Morpion(client, localPlayer));
            }
        });
        btnBattleShip.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new BattleShip(client, localPlayer));
            }
        });

        btnBack.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                client.close();
                ((Game) Gdx.app.getApplicationListener()).setScreen(new LoginScreen());
            }
        });

        btnQuit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.exit(0);
            }
        });
        
        tableMenu.add(new Label("Bienvenue " + localPlayer.getPseudo(), skin,"title",Color.WHITE)).colspan(2);
        tableMenu.row();
        tableMenu.row();
        tableMenu.add(btnMoripon).colspan(2);
        tableMenu.row();
        tableMenu.add(btnBattleShip).colspan(2);
        tableMenu.row();
        tableMenu.add(btnBack);
        tableMenu.add(btnQuit);

        stage.addActor(tableMenu);

        ((OrthographicCamera)stage.getCamera()).zoom = Util.getRatio()/1.5f;

        Gdx.input.setInputProcessor(stage);

        lostConnection = false;

    }

    @Override
    public void render(float delta) {
        if(!lostConnection) {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ApplicationSkin.getInstance().showBackground();
            stage.draw();
        }
        else{
            ((Game) Gdx.app.getApplicationListener()).setScreen(new LoginScreen());
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }
}
