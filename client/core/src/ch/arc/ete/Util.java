package ch.arc.ete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * Util.java   :    collection of util function and infos
 *                  all methods and fields are statics so they can be called without
  *                 Creating an Util object
 * ---------------------------------------------------------------------------------------------
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
