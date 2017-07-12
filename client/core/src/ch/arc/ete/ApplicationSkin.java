package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by jonathan.guerne on 11.07.2017.
 */
public class ApplicationSkin {

    private Skin skin;
    private Texture texture;
    private SpriteBatch batch;

    private static ApplicationSkin ourInstance;

    public static ApplicationSkin getInstance() {
        if(ourInstance == null){
            ourInstance = new ApplicationSkin();
        }
        return ourInstance;
    }

    private ApplicationSkin() {
        skin = new Skin(Gdx.files.internal("skin/comic/comic-ui.json"));
        texture = new Texture(Gdx.files.internal("background.jpg"));
        batch = new SpriteBatch();
    }


    public Skin getSkin() {
        return skin;
    }

    public void showBackground()
    {
        batch.begin();
        batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }
}
