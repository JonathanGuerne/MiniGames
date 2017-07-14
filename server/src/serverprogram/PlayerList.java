package serverprogram;

import java.util.ArrayList;
/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * PlayerList.java   :  list of Player object add more functionality than a basic list such as
  *                     search element by id (not index)
 * ---------------------------------------------------------------------------------------------
 */

public class PlayerList {

    private ArrayList<Player> listPlayer;

    public PlayerList() {
        this.listPlayer = new ArrayList<>();
    }

    public void add(Player p) {
        this.listPlayer.add(p);
    }

    /**
     * return a player giving his index in the list
     *
     * @param index index in the list
     * @return player
     */
    public Player getPlayerAt(int index) {
        return listPlayer.get(index);
    }

    /**
     * get a player knowing his id
     *
     * @param id id of the player
     * @return player
     */
    public Player getPlayerById(int id) {
        Player player = null;

        for (Player p : listPlayer) {
            if (p.getIdPlayer() == id) {
                player = p;
            }
        }

        return player;


    }

    /**
     * check if a player with a specific id is in the player list
     * @param id player id
     * @return true or false in function
     */
    public boolean exist(int id) {
        for(Player p:listPlayer){
            if(p.getIdPlayer() == id){
                return true;
            }
        }
        return false;
    }
}

