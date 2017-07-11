package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by jonathan.guerne on 11.07.2017.
 */
public class ApplicationSkin {

    Skin skin;

    private static ApplicationSkin ourInstance;

    public static ApplicationSkin getInstance() {
        if(ourInstance == null){
            ourInstance = new ApplicationSkin();
        }
        return ourInstance;
    }

    private ApplicationSkin() {
        skin = new Skin(Gdx.files.internal("skin/comic/comic-ui.json"));
    }

    public Skin getSkin() {
        return skin;
    }
}
