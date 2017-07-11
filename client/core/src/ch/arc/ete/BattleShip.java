package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.BattleShip.BattleShipInGamePacket;
import packets.BattleShip.BattleShipStartConfirmPacket;
import packets.BattleShip.BattleShipStartInitGamePacket;
import packets.BattleShip.BattleShipStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class BattleShip extends GameScreen {


    private final int TAB_PLAYER = 0;
    private final int TAB_OPPONENT = 1;
    private ShapeRenderer shapeRenderer;
    private char charUser;
    private final int NB_CASE = 8;
    private final int NB_SHIP = 3;
    private boolean showInit;
    private int shipInitialized;
    private boolean initGame;

    int touchIndex = -1;

    public BattleShip(Client client, Player localPlayer) {
        super(client, localPlayer);
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        w = Gdx.graphics.getWidth() / NB_CASE;
        h = gameLayoutHeight / NB_CASE;
        shapeRenderer.setColor(Color.BLACK);

        tabGame = new HashMap<Integer, char[]>();
        tabGame.put(TAB_PLAYER, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_OPPONENT, new char[0]);

       // Gdx.input.setInputProcessor(this);

        BattleShipStartPacket bssp = new BattleShipStartPacket();
        bssp.idPlayer = localPlayer.getId();

        client.sendTCP(bssp);

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof BattleShipStartConfirmPacket) {
                        BattleShipStartConfirmPacket bsscp = (BattleShipStartConfirmPacket) o;
                        foundOpponent = true;
                        currentPlayerId = bsscp.idPlayer1;

                        if (localPlayer.getId() == bsscp.idPlayer1) {
                            charUser = bsscp.charPlayer1;
                            opponentPlayer = new Player(bsscp.idPlayer2, "Jules");
                        } else {
                            charUser = bsscp.charPlayer2;
                            opponentPlayer = new Player(bsscp.idPlayer1, "Jules");
                        }
                        System.out.println("oposant " + bsscp.idPlayer2 + " " + bsscp.idPlayer1);
                        gameId = bsscp.gameId;

                        initGame = true;
                        showInit = true;
                        showMessage = true;
                        shipInitialized = 0;
                        System.out.println("J'ai recu un packet de confirm");
                    } else if (o instanceof BattleShipInGamePacket)
                    {
                        String text;
                        BattleShipInGamePacket bsigp = (BattleShipInGamePacket) o;
                        currentPlayerId = bsigp.currentPlayerId;
                        if(localPlayer.getId() == currentPlayerId)
                        {
                            text = "C'est votre tour";
                            tabGame.put(TAB_PLAYER, bsigp.currentPlayerTab);
                            tabGame.put(TAB_OPPONENT, bsigp.opponentPlayerTab);
                            text += tabGame.get(TAB_OPPONENT).length;
                        }else
                        {
                            text = "C'est le tour de votre adversaire";
                        }
                        setCenterText(text);
                        showMessage = true;
                    }
                }
            }
        });
    }

    @Override
    public void display() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ApplicationSkin.getInstance().showBackground();

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabGame.get(TAB_PLAYER).length; i++) {
            if (tabGame.get(TAB_PLAYER)[i] == charUser) {
                int x = i % NB_CASE;
                int y = 7 - (i / NB_CASE);
                shapeRenderer.rect(x * w, y * h, w, h);
            }
        }
        shapeRenderer.end();

        if(tabGame.get(TAB_OPPONENT).length != 0)
        {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < tabGame.get(TAB_OPPONENT).length; i++) {
                if (tabGame.get(TAB_OPPONENT)[i] == charUser) {
                    int x = i % NB_CASE;
                    int y = 7 - (i / NB_CASE);
                    shapeRenderer.rect(x * w, y * h, w, h);
                }
                System.out.println("je m'appele toto");
            }
            shapeRenderer.end();
        }

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NB_CASE; i++) {
            shapeRenderer.line(i * w, 0, i * w, gameLayoutHeight);
            shapeRenderer.line(0, i * h, Gdx.graphics.getWidth(), i * h);
        }
        shapeRenderer.end();

        if (initGame) {
            setCenterText("Init de la partie");

            if (shipInitialized >= NB_SHIP) {
                System.out.println("Tu as dépasser le bord des limites " + charUser);
            }
        }

        if (showMessage) {
            float x = 0;
            float y = Gdx.graphics.getHeight() / 2 + layout.height / 2;
            batch.begin();
            font.draw(batch, layout, x, y);
            batch.end();
        }
    }

    @Override
    public void update() {
        if (initGame && shipInitialized == NB_SHIP) {
            BattleShipStartInitGamePacket bssigp = new BattleShipStartInitGamePacket();
            bssigp.idPlayer = localPlayer.getId();
            System.out.println("opponent " + opponentPlayer.getId());
            bssigp.idOpponent = opponentPlayer.getId();
            bssigp.tabGame = tabGame.get(TAB_PLAYER);
            bssigp.gameId = gameId;

            client.sendTCP(bssigp);
            initGame = false;

            setCenterText("Attente de l'autre joueur...");
            showMessage = true;
        }
    }

    @Override
    public void resize(int width, int height) {
        w = Gdx.graphics.getWidth() / NB_CASE;
        h = gameLayoutHeight / NB_CASE;
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
        shapeRenderer.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (initGame && !showMessage) {

            if (screenY < informationLayoutHeight) {
                return false;
            }
            screenY -= informationLayoutHeight;
            shipInitialized++;
            int x = (int) (screenX / w);
            int y = (NB_CASE * (int) (screenY / h));
            touchIndex = x + y;
            tabGame.get(TAB_PLAYER)[touchIndex] = charUser;
            System.out.println(touchIndex);


            //System.out.println("J'ai toucher " + screenX);

        } else if (gameOver) {
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, this.localPlayer));
        } else {

        }

        if (showMessage) {
            showMessage = false;
            setCenterText("");
        }
        return false;
    }


    @Override
    protected void setGameMenu() {
        TextButton btnInvert = new TextButton("Inverser",skin);

        btnInvert.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //todo : handle button invert clicked
            }
        });

        tableDisplay.add(btnInvert);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
