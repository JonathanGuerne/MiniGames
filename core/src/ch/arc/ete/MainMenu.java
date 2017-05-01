package ch.arc.ete;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.Packet;


public class MainMenu implements Screen, InputProcessor {
    SpriteBatch batch;
    private BitmapFont font;

    Client client;

    static int tcp = 23900, udp = 23901;

    String text = "the answer is : ?";

    public MainMenu(Client client) {
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

        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, text, 100, 100);
        batch.end();
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
        System.out.println("touch");
        MiniGamePacket mp = new MiniGamePacket();
        mp.answer = client.getID();
        client.sendUDP(mp);
        return true;
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
