package serverprogram;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * GameChar.java   : use by the morpionhandler to share the winner char
 *                   can be use later to add information about the game (more than juste a Character)
 * ---------------------------------------------------------------------------------------------
 */
public class GameChar {
    private char infoChar;

    public GameChar(char infoChar) {
        this.infoChar = infoChar;
    }

    public GameChar() {
    }

    public char getInfoChar() {
        return infoChar;
    }

    public void setInfoChar(char infoChar) {
        this.infoChar = infoChar;
    }
}
