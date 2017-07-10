package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.BattleShip.BattleShipInGamePacket;
import packets.BattleShip.BattleShipInitGamePacket;
import packets.BattleShip.BattleShipStartConfirmPacket;
import packets.BattleShip.BattleShipStartPacket;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class BattleShip extends GameScreen implements InputProcessor {

    ShapeRenderer shapeRenderer;
    float w, h;
    char charUser;
    final int NB_CASE = 8;
    final int NB_SHIP = 10;
    boolean showInit;
    int shipInitialized;
    char[] opponentTab;

    int touchIndex = -1;

    public BattleShip(Client client, Player localPlayer) {
        super(client, localPlayer);
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        w = gameLayoutWith / NB_CASE;
        h = Gdx.graphics.getHeight() / NB_CASE;
        shapeRenderer.setColor(Color.BLACK);

        tabGame = new HashMap<Integer, char[]>();
        tabGame.put(0, new char[NB_CASE * NB_CASE]);

        Gdx.input.setInputProcessor(this);

        BattleShipStartPacket bssp = new BattleShipStartPacket();
        /*J regarde la
        *
        *
        *
        * est ce que j'utilise client.getID ou localPlayer ????
        * */
        bssp.idPlayer = localPlayer.getId();

        client.sendTCP(bssp);

        client.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object o) {
                if(o instanceof BattleShipStartConfirmPacket) {
                    BattleShipStartConfirmPacket bsscp = (BattleShipStartConfirmPacket) o;
                    foundOpponent = true;
                    currentPlayerID = bsscp.idPlayer1;

                    if (localPlayer.getId() == bsscp.idPlayer1) {
                        charUser = bsscp.charPlayer1;
                        opponentPlayer = new Player(bsscp.idPlayer2, "Jules");
                    } else {
                        charUser = bsscp.charPlayer2;
                        opponentPlayer = new Player(bsscp.idPlayer1, "Jules");
                    }

                    gameId = bsscp.gameId;

                    opponentTab = new char[NB_CASE * NB_CASE];
                    initGame = true;
                    showInit = true;
                    shipInitialized = 0;
                    System.out.println("J'ai recu un packet de confirm");
                }else if(o instanceof BattleShipInitGamePacket)
                {

                }
            }
        });
    }

    @Override
    public void display() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabGame.get(0).length; i++) {
            if (tabGame.get(0)[i] == charUser) {
                int x = i % NB_CASE;
                int y = 7 - (i / NB_CASE);
                shapeRenderer.rect(x*w, y*h, w, h);
            }
        }
        shapeRenderer.end();

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NB_CASE; i++)
        {
            shapeRenderer.line(i * w, 0, i * w, Gdx.graphics.getHeight());
            shapeRenderer.line(0, i * h, gameLayoutWith, i * h);
        }
        shapeRenderer.end();

        if(initGame)
        {
            if(showInit)
            {
                setCenterText("Init de la partie");

            }
            if(shipInitialized >= NB_SHIP)
            {
                System.out.println("Tu as dépasser le bord des limites " + charUser);
            }
        }
        float x = 0;
        float y = Gdx.graphics.getHeight()/2 + layout.height/2;

        batch.begin();
        font.draw(batch,layout,x,y);
        batch.end();
    }

    @Override
    public void update()
    {
        if(initGame && shipInitialized == NB_SHIP)
        {

            BattleShipInGamePacket bsigp = new BattleShipInGamePacket();
            bsigp.currentPlayerId = localPlayer.getId();
            bsigp.opponentPlayerId = opponentPlayer.getId();
            bsigp.currentPlayerTab = tabGame.get(0);
            bsigp.opponentPlayerTab = opponentTab;
            bsigp.gameId = gameId;

            client.sendTCP(bsigp);

            initGame = false;

            setCenterText("Attente de l'autre joueur");
        }
    }

    @Override
    public void resize(int width, int height)
    {
        w = gameLayoutWith / NB_CASE;
        h = Gdx.graphics.getHeight() / NB_CASE;
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
    public void dispose()
    {
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
        if(initGame)
        {
            if(showInit)
            {
                showInit = false;
            }else
            {
                if(screenX > gameLayoutWith)
                {
                    return false;
                }
                shipInitialized++;
                int x = (int) (screenX / w);
                int y = (NB_CASE * (int) (screenY / h));
                touchIndex = x + y;
                tabGame.get(0)[touchIndex] = charUser;
                System.out.println(touchIndex);
            }

            //System.out.println("J'ai toucher " + screenX);

        }else if(gameOver)
        {
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, this.localPlayer));
        }else
        {

        }

        return false;
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
