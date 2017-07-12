package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Created by jonathan.guerne on 22.04.2017.
 */

public class Util {


    public static final String skinName = "skin/comic/comic-ui.json" ;

    public static BitmapFont createFont(int dp) {
        FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/animeace2_reg.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = dp;
        return ftfg.generateFont(parameter);
    }

    public static float getRatio(){
        return Gdx.graphics.getWidth()/1024;
    }
}
