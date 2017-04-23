package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Created by jonathan.guerne on 22.04.2017.
 */

public class Util {
    public static BitmapFont createFont(int dp) {
        FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/04B_19__.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = dp;
        return ftfg.generateFont(parameter);
    }
}
