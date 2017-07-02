package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.LoginConfirmPacket;
import packets.MorpionEndGamePacket;
import packets.MorpionInGameConfirmPacket;
import packets.MorpionInGamePacket;
import packets.MorpionStartConfirmPacket;
import packets.MorpionStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class Morpion extends GameScreen implements InputProcessor {

    ShapeRenderer shapeRenderer;
    float w, h;
    char charUser;

    int touchIndex = -1;


    public Morpion(Client client) {
        super(client);

    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        w = Gdx.graphics.getWidth() / 3;
        h = Gdx.graphics.getHeight() / 3;
        shapeRenderer.setColor(Color.BLACK);


        tabGame = new HashMap<Integer, char[]>();

        tabGame.put(0,new char[9]);

        Gdx.input.setInputProcessor(this);

        MorpionStartPacket msp = new MorpionStartPacket();
        msp.idPlayer = localPlayer.getId();

        client.sendTCP(msp);

        client.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object o) {
                if(o instanceof Packet) {
                    if (o instanceof MorpionStartConfirmPacket) {
                        MorpionStartConfirmPacket mscp = (MorpionStartConfirmPacket) o;
                        foundOpponent = true;
                        currentPlayerID = mscp.idPlayer1;

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
                        currentPlayerID = migcp.currentPlayerID;
                    } else if (o instanceof MorpionEndGamePacket) {
                        MorpionEndGamePacket megp = (MorpionEndGamePacket) o;
                        tabGame.put(0, megp.tabGame);
                        gameOver = true;
                        winnerId = megp.winnerId;
                        System.out.println("My id is "+localPlayer.getId());
                    }
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
            if (tabGame.get(0)[i] == 'x') {
                int x = i%3;
                int y = 2-(i/3);
                shapeRenderer.rect(x*w, y*h,w,h);
            }
        }
        shapeRenderer.end();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabGame.get(0).length; i++) {
            if (tabGame.get(0)[i] == 'o') {
                int x = i%3;
                int y = 2-(i/3);
                shapeRenderer.rect(x*w, y*h,w,h);
            }
        }
        shapeRenderer.end();

        shapeRenderer.setColor(Color.BLACK);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < 4; i++) {
            shapeRenderer.line(i * w, 0, i * w, Gdx.graphics.getHeight());
            shapeRenderer.line(0, i * h, Gdx.graphics.getWidth(), i * h);
        }

        shapeRenderer.end();

        if(gameOver){
            if(winnerId == localPlayer.getId()){
                setCenterText("Vous avez gagne");
            }
            else if(winnerId == -1){
                setCenterText("egalite");
            }
            else{
                setCenterText("Vous avez perdu");
            }

            float x = 0;
            float y = Gdx.graphics.getHeight()/2 + layout.height/2;

            batch.begin();
            font.draw(batch,layout,x,y);
            batch.end();
        }

    }

    @Override
    public void update() {
        if(!gameOver) {
            if (currentPlayerID == localPlayer.getId()) {
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
        }
    }



    @Override
    public void resize(int width, int height) {
        w = Gdx.graphics.getWidth() / 3;
        h = Gdx.graphics.getHeight() / 3;
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
            int x = (int) (screenX / w);
            int y = (3 * (int) (screenY / h));
            touchIndex = x + y;
            System.out.println(touchIndex);
            return false;
        }
        else{
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client));
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
