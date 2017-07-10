package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 27.04.2017.
 */
public class LoginScreen implements Screen {

    Label serverLabel;
    Label pseudoLabel;
    TextButton btnValider;
    TextField serverAdress;
    TextField clientPseudo;
    Stage stage;
    SpriteBatch sb;
    OrthographicCamera cam;
    Client client;

    static int tcp = 23900, udp = 23901;

    boolean connectionOk = false;


    @Override
    public void show() {

        sb = new SpriteBatch();
        stage = new Stage();

        ClientBuilder cb = new ClientBuilder();
        client = cb.build(new Client());
        client.start();

        Table tableDisplay = new Table();

        tableDisplay.setPosition(0, 0);

        tableDisplay.setWidth(Gdx.graphics.getWidth());
        tableDisplay.setHeight(Gdx.graphics.getHeight());

        tableDisplay.center();


        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));


        serverLabel = new Label("Adresse du server : ", skin);

        pseudoLabel = new Label("Pseudo : ", skin);

        btnValider = new TextButton("Connexion", skin);

        btnValider.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    client.connect(5000,serverAdress.getText(),tcp,udp);
                    LoginPacket lp = new LoginPacket();
                    client.sendTCP(lp);

                    while (!connectionOk){
//                        System.out.println("waiting for server response");
                    }

                    ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, clientPseudo.getText()));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        client.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object o) {
                if(o instanceof Packet){
                    if(o instanceof LoginConfirmPacket){
                        LoginConfirmPacket lcp = (LoginConfirmPacket) o;
                        connectionOk = true;
                    }
                }
            }
        });

        serverAdress = new TextField("127.0.0.1", skin);
        clientPseudo = new TextField("", skin);

        tableDisplay.add(serverLabel);
        tableDisplay.add(serverAdress).width(300);
        tableDisplay.row();
        tableDisplay.add(pseudoLabel);
        tableDisplay.add(clientPseudo).width(300);
        tableDisplay.row();
        tableDisplay.add(btnValider);
        tableDisplay.row();

        stage.addActor(tableDisplay);

        ((OrthographicCamera)stage.getCamera()).zoom = Util.getRatio()/2;

        Gdx.input.setInputProcessor(stage);

    }


    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        ((OrthographicCamera)stage.getCamera()).zoom = Util.getRatio()/2;
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

    }
}
