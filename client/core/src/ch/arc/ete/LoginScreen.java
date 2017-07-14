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

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * LoginScreen.java   :  Connexion screen open at the start of the program
 * ---------------------------------------------------------------------------------------------
 */
public class LoginScreen implements Screen {

    private Label serverLabel;
    private Label pseudoLabel;
    private Label errorLabel;
    private Label serversListLabel;
    private TextButton btnValider;
    private TextField serverAdress;
    private TextField clientPseudo;
    private SelectBox<String> serversAdresses;
    private ImageButton btnRefreshServersList;

    private Stage stage;
    private SpriteBatch sb;
    private OrthographicCamera cam;
    private Client client;

    static int tcp = 23900, udp = 23901;

    private boolean connectionOk = false;

    /**
     * show is called  1 time when the screen appears
     * it's like a constructor
     */
    @Override
    public void show() {

        sb = new SpriteBatch();
        stage = new Stage();

        //the client builder is use to register all the needed packet
        ClientBuilder cb = new ClientBuilder();

        //create a client object (class from kryonet) and give him the registered packets
        client = cb.build(new Client());
        client.start();

        //tableDisplay is use to manage the disposition of graphical elements in the screen
        Table tableDisplay = new Table();

        tableDisplay.setPosition(0, 0);

        tableDisplay.setWidth(Gdx.graphics.getWidth());
        tableDisplay.setHeight(Gdx.graphics.getHeight());

        tableDisplay.center();


        //the skin is use to personalize all graphical element from the screen
        Skin skin = ApplicationSkin.getInstance().getSkin();

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

        /**
         * Discover server in background so the client can still use the app
         */
        class DiscoverHostThread implements Runnable {

            @Override
            public void run() {

                Array<String> listAddressesString = new Array<String>();
                java.util.List<InetAddress> listAddresses;

                //attempt to find server on tcp port
                listAddresses = client.discoverHosts(tcp, 3000);
                for (InetAddress adr : listAddresses) {
                    if (!listAddressesString.contains(adr.getHostAddress(), false)) {
                        listAddressesString.add(adr.getHostAddress());
                    }
                }
                //attempt to find server on udp port
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
                }

            }
        }

        //start the server discovering
        new Thread(new DiscoverHostThread()).start();

        /**
         * event call on button "Connexion" clicked
         */
        btnValider.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryConnection();
            }
        });

        /**
         * event call when the selected item from the server list change
         */
        serversAdresses.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                serverAdress.setText(serversAdresses.getSelected());
            }
        });

        /**
         * event call on button "Refresh" clicked
         */
        btnRefreshServersList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new Thread(new DiscoverHostThread()).start();
            }
        });

        /**
         * this part of the code is use to received packet from the server
         */
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                //first check if it's a packet
                if (o instanceof Packet) {
                    //then check what instance of packet it is
                    if (o instanceof LoginConfirmPacket) {
                        LoginConfirmPacket lcp = (LoginConfirmPacket) o;
                        clientPseudo.setText(lcp.namePlayer);
                        connectionOk = true;
                    }
                }
            }
        });

        serverAdress = new TextField("127.0.0.1", skin);
        clientPseudo = new TextField("", skin);

        clientPseudo.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\r') {
                    tryConnection();
                }
            }
        });


        serverAdress.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\r') {
                    tryConnection();
                }
            }
        });

        //putting all graphical elements in the table

        tableDisplay.add(new Label("MINI GAMES", skin, "title", Color.WHITE)).colspan(3);
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
        stage.setKeyboardFocus(clientPseudo);


        //applaying a zoom to the interface based on the ratio of the screen (useful for mobile devices)
        ((OrthographicCamera) stage.getCamera()).zoom = Util.getRatio() / 1.5f;

        //bind the input with graphical elements in the interface
        Gdx.input.setInputProcessor(stage);

    }

    /**
     * attempt to connect to the server
     * the address use is the one write in the textfield
     * if the user didn't write a pseudo the program won't let him connect to the server
     */
    private void tryConnection() {
        try {
            if (clientPseudo.getText().equals("")) {
                throw new LoginException("Veuillez choisir un pseudo");
            } else if (clientPseudo.getText().length() > 20) {
                throw new LoginException("Votre pseudo ne doit pas contenir\n plus que 20 caractères !");
            }
            client.connect(5000, serverAdress.getText(), tcp, udp);
            LoginPacket lp = new LoginPacket();
            lp.namePlayer = clientPseudo.getText();
            client.sendTCP(lp);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        if(!connectionOk){
                            errorLabel.setText("pas de reponses, verifiez les mises à jour.");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }).start();

        } catch (IOException e) {
            errorLabel.setText("Serveur inconnu");
            e.printStackTrace();
        } catch (LoginException e) {
            errorLabel.setText(e.getMessage());
        }
    }


    /**
     * displaying screen content
     * @param delta
     */
    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ApplicationSkin.getInstance().showBackground();

        stage.act();
        stage.draw();

        if(connectionOk){
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, clientPseudo.getText()));
        }
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
