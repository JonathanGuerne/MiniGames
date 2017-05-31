package ch.arc.ete;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.Packet;


public class MainMenu implements Screen {
    SpriteBatch batch;
    private BitmapFont font;

    Client client;

    static int tcp = 23900, udp = 23901;

    String text = "the answer is : ?";

    Table tableMenu;

    Stage stage;

    TextButton btnMoripon;

    public MainMenu(final Client client) {
        this.client = client;
    }

    @Override
    public void show() {

        client.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                    if(object instanceof Packet){
                        System.out.println("here");
                        if(object instanceof MiniGamePacket){
                            MiniGamePacket mp = (MiniGamePacket) object;
                            //text = "The answer is : "+mp.answer;
                        }
                    }

            }

            @Override
            public void disconnected(Connection connection) {

            }

        });


        batch = new SpriteBatch();
        font = Util.createFont(72);
        font.setColor(Color.RED);

        tableMenu = new Table();
        stage = new Stage();

        tableMenu.setPosition(0,0);
        tableMenu.setHeight(Gdx.graphics.getHeight());
        tableMenu.setWidth(Gdx.graphics.getWidth());

        tableMenu.center();

        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        btnMoripon = new TextButton("Jouer au morpion",skin);

        btnMoripon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new Morpion(client));
            }
        });

        tableMenu.add(new Label("Menu Principal",skin));
        tableMenu.row();
        tableMenu.add(btnMoripon);

        stage.addActor(tableMenu);

        ((OrthographicCamera)stage.getCamera()).zoom = Util.getRatio()/2;

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
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
        font.dispose();
    }
}
