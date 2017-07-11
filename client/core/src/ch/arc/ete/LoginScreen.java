package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 27.04.2017.
 */
public class LoginScreen implements Screen {

    Label serverLabel;
    Label pseudoLabel;
    Label errorLabel;
    Label serversListLabel;
    TextButton btnValider;
    TextField serverAdress;
    TextField clientPseudo;
    SelectBox<String> serversAdresses;
    ImageButton btnRefreshServersList;

    Stage stage;
    SpriteBatch sb;
    OrthographicCamera cam;
    Client client;

    static int tcp = 23900, udp = 23901;

    boolean connectionOk = false;
//    boolean serverDiscoveringFinish = false;


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


        Skin skin = ApplicationSkin.getInstance().getSkin();

//        serversListLabel = new Label("Liste des serveurs : ",skin);

        serversListLabel = new Label("Liste des serveurs : ", skin);

        serverLabel = new Label("Adresse du server : ", skin);

        pseudoLabel = new Label("Pseudo : ", skin);

        errorLabel = new Label("", skin);
        errorLabel.setColor(255, 0, 0, 255);

        btnValider = new TextButton("Connexion", skin);

        btnRefreshServersList = new ImageButton(skin);
        btnRefreshServersList.getStyle().imageUp = new TextureRegionDrawable(
                new TextureRegion(
                        new Texture(Gdx.files.internal("refresh.png"))));


        serversAdresses = new SelectBox<String>(skin);
        serversAdresses.setDisabled(true);


        System.out.println("Discovering Hosts...");

        class DiscoverHostThread implements Runnable {

            @Override
            public void run() {

                Array<String> listAddressesString = new Array<String>();
                java.util.List<InetAddress> listAddresses;

                listAddresses = client.discoverHosts(tcp, 3000);
                for (InetAddress adr : listAddresses) {
                    if (!listAddressesString.contains(adr.getHostAddress(), false)) {
                        listAddressesString.add(adr.getHostAddress());
                    }
                }
                listAddresses = client.discoverHosts(udp, 2000);
                for (InetAddress adr : listAddresses) {
                    if (!listAddressesString.contains(adr.getHostAddress(), false)) {
                        listAddressesString.add(adr.getHostAddress());
                    }
                }

                if (listAddressesString.size == 0) {

                } else {
                    serversAdresses.setItems(listAddressesString);
                    serversAdresses.setDisabled(false);
//                    serverDiscoveringFinish = true;
                }

            }
        }

        new Thread(new DiscoverHostThread()).start();

        btnValider.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (clientPseudo.getText().equals("")) {
                    errorLabel.setText("Veuillez choisir un pseudo");
                    return;
                }
                try {
                    client.connect(5000, serverAdress.getText(), tcp, udp);
                    LoginPacket lp = new LoginPacket();
                    client.sendTCP(lp);

                    while (!connectionOk) {
//                        System.out.println("waiting for server response");
                    }

                    ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, clientPseudo.getText()));


                } catch (IOException e) {
                    errorLabel.setText("Serveur inconnu");
                    e.printStackTrace();
                }
            }
        });


        serversAdresses.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
//                if (serverDiscoveringFinish) {
                    serverAdress.setText(serversAdresses.getSelected());
//                }
            }
        });


        btnRefreshServersList.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new Thread(new DiscoverHostThread()).start();
            }
        });

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof LoginConfirmPacket) {
                        LoginConfirmPacket lcp = (LoginConfirmPacket) o;
                        connectionOk = true;
                    }
                }
            }
        });

        serverAdress = new TextField("127.0.0.1", skin);
        clientPseudo = new TextField("", skin);

        tableDisplay.add(new Label("MINI GAMES",skin,"title",Color.BLACK)).colspan(3);
        tableDisplay.row();
        tableDisplay.add(serverLabel).align(Align.left);
        tableDisplay.add(serverAdress).width(200);
        tableDisplay.row();
        tableDisplay.add(pseudoLabel).align(Align.left);
        tableDisplay.add(clientPseudo).width(200);
        tableDisplay.row();
        tableDisplay.row();
        tableDisplay.add(serversListLabel).align(Align.left);
        tableDisplay.add(serversAdresses).width(200);
        tableDisplay.add(btnRefreshServersList);
        tableDisplay.row();
        tableDisplay.add(errorLabel).colspan(3);
        tableDisplay.row();
        tableDisplay.add(btnValider).width(250).colspan(3);
        tableDisplay.row();

        stage.addActor(tableDisplay);

        ((OrthographicCamera) stage.getCamera()).zoom = Util.getRatio() / 1.5f;

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
        ((OrthographicCamera) stage.getCamera()).zoom = Util.getRatio() / 1.5f;
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
