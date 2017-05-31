package ch.arc.ete;

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
import packets.MorpionStartConfirmPacket;
import packets.MorpionStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class Morpion extends GameScreen implements InputProcessor {

    ShapeRenderer shapeRenderer;
    float w, h;


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
        msp.idPlayer = localPlayerId;

        client.sendTCP(msp);

        client.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object o) {
                if(o instanceof Packet){
                    if(o instanceof MorpionStartConfirmPacket){
                        foundOpponent = true;
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
                int y = i/3;
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
    }

    @Override
    public void update() {
        System.out.println("update stuff");
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

        System.out.println("screen x : " + screenX + " screen y : " + screenY + "");
        int x = (int)(screenX/w);
        int y = (3*(2-(int)(screenY/h)));
        int index = x+y;
        System.out.println("index : "+index);
        tabGame.get(0)[index] = 'x';
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
