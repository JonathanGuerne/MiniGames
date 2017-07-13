package ch.arc.ete;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * LoginEXception.java   : use to handle login exception
 * ---------------------------------------------------------------------------------------------
 */

public class LoginException extends Exception {

    public LoginException(String message)
    {
        super(message);
    }
}
