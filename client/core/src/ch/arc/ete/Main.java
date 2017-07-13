package ch.arc.ete;

import com.badlogic.gdx.Game;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * Main.java   :    extending game, lauch at the start of the program
 *                  use screen.
 * ---------------------------------------------------------------------------------------------
 */

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new LoginScreen());
    }
}
