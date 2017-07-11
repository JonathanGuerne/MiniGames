package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import javax.swing.JLabel;

import packets.MorpionEndGamePacket;
import packets.MorpionInGameConfirmPacket;
import packets.MorpionInGamePacket;
import packets.MorpionPlayerLeaving;
import packets.MorpionStartConfirmPacket;
import packets.MorpionStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class Morpion extends GameScreen {

    ShapeRenderer shapeRenderer;

    Sprite imageArray[] = new Sprite[9];

    float w, h;
    char charUser;

    int touchIndex = -1;

    Label currentPlayerPseudo;


    public Morpion(Client client, Player localPlayer) {
        super(client, localPlayer);
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        w =  Gdx.graphics.getWidth() / 3;
        h = gameLayoutHeight / 3;
        shapeRenderer.setColor(Color.BLACK);


        tabGame = new HashMap<Integer, char[]>();

        tabGame.put(0, new char[9]);

        MorpionStartPacket msp = new MorpionStartPacket();
        msp.idPlayer = localPlayer.getId();

        client.sendTCP(msp);

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof MorpionStartConfirmPacket && !foundOpponent) {

                        MorpionStartConfirmPacket mscp = (MorpionStartConfirmPacket) o;

                        foundOpponent = true;
                        currentPlayerId = mscp.idPlayer1;

                        if (localPlayer.getId() == mscp.idPlayer1) {
                            charUser = mscp.charPlayer1;
                            opponentPlayer = new Player(mscp.idPlayer2, "Jules");
                        } else {
                            charUser = mscp.charPlayer2;
                            opponentPlayer = new Player(mscp.idPlayer1, "Jules");
                        }

                        gameId = mscp.gameId;

                    } else if (o instanceof MorpionInGameConfirmPacket) {
                        MorpionInGameConfirmPacket migcp = (MorpionInGameConfirmPacket) o;
                        touchIndex = -1;
                        tabGame.put(0, migcp.tabGame);
                        currentPlayerId = migcp.currentPlayerID;
                    } else if (o instanceof MorpionEndGamePacket) {
                        MorpionEndGamePacket megp = (MorpionEndGamePacket) o;
                        tabGame.put(0, megp.tabGame);
                        gameOver = true;
                        winnerId = megp.winnerId;
                    }
                }

            }
        });

    }


    /**
     * method call when the player choose to left
     * todo: tell the server
     */
    @Override
    public void playerLeft() {
        MorpionPlayerLeaving mpl = new MorpionPlayerLeaving();
        mpl.playerid = localPlayer.getId();
        mpl.playerName = localPlayer.getPseudo();
        client.sendTCP(mpl);
    }

    @Override
    public void display() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ApplicationSkin.getInstance().showBackground();

        if(playerTurn != null) {
            String playerPlaying = (currentPlayerId == localPlayer.getId()) ? "Votre tour " : "Tour de l'adversaire";
            playerTurn.setText(playerPlaying);
        }

        if (gameLoaded) {
            stage.act();
            stage.draw();
        }


//        shapeRenderer.setColor(Color.GREEN);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        for (int i = 0; i < tabGame.get(0).length; i++) {
//            if (tabGame.get(0)[i] == 'x') {
//                int x = i%3;
//                int y = 2-(i/3);
//                shapeRenderer.rect(x*w, y*h,w,h);
//            }
//        }
//        shapeRenderer.end();
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        for (int i = 0; i < tabGame.get(0).length; i++) {
//            if (tabGame.get(0)[i] == 'o') {
//                int x = i%3;
//                int y = 2-(i/3);
//                shapeRenderer.rect(x*w, y*h,w,h);
//            }
//        }
//        shapeRenderer.end();

        shapeRenderer.setColor(Color.BLACK);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < 4; i++) {
            shapeRenderer.line(i * w, 0, i * w, gameLayoutHeight);
            shapeRenderer.line(0, i * h, Gdx.graphics.getWidth(), i * h);
        }

        shapeRenderer.end();

        batch.begin();

        for (int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] != null) {
                imageArray[i].draw(batch);
            }
        }

        batch.end();

    }

    @Override
    public void update() {

        for (int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] == null && tabGame.get(0)[i] != '\0') {
                int x = i % 3;
                int y = 2 - (i / 3);
                Texture tex = new Texture(Gdx.files.internal("morpion/" + tabGame.get(0)[i] + ".png"));
                imageArray[i] = new Sprite(tex);
                imageArray[i].setX(x*w);
                imageArray[i].setY(y*h);
                imageArray[i].setSize(w,h);
            }
        }

        if (!gameOver) {
            if (currentPlayerId == localPlayer.getId()) {
                //if touchIndex is a value >= 0 and tab[touchIndex] is null
                if (touchIndex != -1 && tabGame.get(0)[touchIndex] == '\0') {
                    tabGame.get(0)[touchIndex] = charUser;
                    MorpionInGamePacket migp = new MorpionInGamePacket();
                    migp.currentPlayerChar = charUser;
                    migp.currentPlayerID = localPlayer.getId();
                    migp.opponentPlayerID = opponentPlayer.getId();
                    migp.tabGame = tabGame.get(0);
                    migp.gameId = gameId;

                    client.sendTCP(migp);
                }
            }
        } else {
            if (winnerId == localPlayer.getId()) {
                setCenterText("Vous avez gagne");
            } else if (winnerId == -1) {
                setCenterText("egalite");
            } else {
                setCenterText("Vous avez perdu");
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        w = Gdx.graphics.getWidth() / 3;
        h = gameLayoutHeight / 3;
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
        System.out.println("X : " + screenX + ". Y : " + screenY);
        if(!gameOver) {
            if(screenY < informationLayoutHeight)
            {
                return false;
            }
            int x = (int) (screenX / w);
            int y = (3 * (int) (screenY / h));
            //Fix round problem
            if(x + y >= 0 && x + y < tabGame.get(0).length) {
                touchIndex = x + y;
            }
            return false;
        } else {
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, localPlayer));
            return false;
        }
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
