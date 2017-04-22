package ch.arc.ete;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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

import packets.MiniGamePacket;
import packets.Packet;


public class MyGame extends ApplicationAdapter {
	SpriteBatch batch;
    private BitmapFont font;

	Client client;

	static int tcp = 23900, udp = 23901;

    String text = "The answer is : ?";

	@Override
	public void create () {
		client = new Client();
        client.start();

        client.getKryo().register(Packet.class,100);
        client.getKryo().register(MiniGamePacket.class,200);

        try {
            client.connect(1000,"192.168.31.17",tcp,udp);
            client.addListener(new Listener(){

                @Override
                public void received(Connection connection, Object object) {
                  if(object instanceof Packet){
                      System.out.println("here");
                      if(object instanceof MiniGamePacket){
                          MiniGamePacket mp = (MiniGamePacket) object;
                          text = "The answer is : "+mp.answer;
                      }
                  }
                }

                @Override
                public void disconnected(Connection connection) {

                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        batch = new SpriteBatch();
        font = Util.createFont(72);
        font.setColor(Color.RED);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        font.draw(batch,text,100,100);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
        font.dispose();
	}
}
