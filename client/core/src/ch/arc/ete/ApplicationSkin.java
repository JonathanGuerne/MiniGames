package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * ApplicationSkin.java   :  this class is a singleton use to get the programme skin
 *                          we used a singleton so that it is only generate one time.
 * ---------------------------------------------------------------------------------------------
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
