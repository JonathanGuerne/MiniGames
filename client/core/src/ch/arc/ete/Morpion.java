package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.morpion.MorpionEndGamePacket;
import packets.morpion.MorpionInGameConfirmPacket;
import packets.morpion.MorpionInGamePacket;
import packets.morpion.MorpionStartConfirmPacket;
import packets.morpion.MorpionStartPacket;
import packets.Packet;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * Morpion.java   : morpion mini game implementation
 * ---------------------------------------------------------------------------------------------
 */

public class Morpion extends GameScreen {

    private ShapeRenderer shapeRenderer;

    private Sprite imageArray[] = new Sprite[9];

    private char charUser;

    private int touchIndex = -1;


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
                        //if the server send a morpionstartconfirmpacket

                        MorpionStartConfirmPacket mscp = (MorpionStartConfirmPacket) o;

                        //found an opponent
                        foundOpponent = true;
                        currentPlayerId = mscp.idPlayer1;


                        if (localPlayer.getId() == mscp.idPlayer1) {
                            charUser = mscp.charPlayer1;
                            opponentPlayer = new Player(mscp.idPlayer2, mscp.player2Name);
                        } else {
                            charUser = mscp.charPlayer2;
                            opponentPlayer = new Player(mscp.idPlayer1, mscp.player1Name);
                        }
                        displayCurrentPlayer(opponentPlayer.getPseudo());
                        gameId = mscp.gameId;

                    } else if (o instanceof MorpionInGameConfirmPacket) {
                        //morpioningameconfirmpacket is a packet send by the server each time a player send him a morpioningamepacket

                        MorpionInGameConfirmPacket migcp = (MorpionInGameConfirmPacket) o;
                        touchIndex = -1;
                        //update the tabgame
                        tabGame.put(0, migcp.tabGame);
                        //change the current player id
                        currentPlayerId = migcp.currentPlayerID;
                        //show a message in the center of the screen
                        displayCurrentPlayer(opponentPlayer.getPseudo());

                    } else if (o instanceof MorpionEndGamePacket) {
                        //if the game is over
                        MorpionEndGamePacket megp = (MorpionEndGamePacket) o;
                        //update the tabgame
                        tabGame.put(0, megp.tabGame);
                        //the game is over
                        gameOver = true;
                        //change the winnerId
                        winnerId = megp.winnerId;
                    }
                }

            }
        });

    }


    @Override
    protected void setGameMenu() {

    }

    @Override
    public void display() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //displaying the background image
        ApplicationSkin.getInstance().showBackground();


        //displaying black lines (horizontal and vertical) use a guide for the player to click on the right case
        shapeRenderer.setColor(Color.BLACK);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 4; i++) {
            shapeRenderer.rectLine(i * w, 0, i * w, gameLayoutHeight,3);
            shapeRenderer.rectLine(0, i * h, Gdx.graphics.getWidth(), i * h,3);
        }

        shapeRenderer.end();

        //displaying the image (red 'x' and blue 'o')
        batch.begin();

        for(int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] != null) {
                imageArray[i].draw(batch);
            }
        }

        batch.end();

        //if there is a message to show
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

        //create a new image if needed
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

        //if the game isn't over
        if (!gameOver) {
            //if it is the turn of the local player
            if (currentPlayerId == localPlayer.getId()) {
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
            //display the correct text based on the winnerId
            if (winnerId == localPlayer.getId()) {
                setCenterText("Vous avez gagne");
            } else if (winnerId == -1) {
                setCenterText("egalite");
            } else if(winnerId == opponentPlayer.getId()){
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

        if(!gameOver) {
            //if the game isn't over
            if(screenY < informationLayoutHeight)
            {
                //if the player click out of the gamescreen
                return false;
            }
            if(!showMessage) {
                //if there is no message to show
                screenY -= informationLayoutHeight;
                int x = (int) (screenX / w);
                int y = (3 * (int) (screenY / h));
                touchIndex = x + y;
            }
            else
            {
                //hide the message
                showMessage = false;
                setCenterText("");
            }
        } else {
            //send the player back to the mainmenu
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, localPlayer));
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
