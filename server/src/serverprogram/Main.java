package serverprogram;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import packets.GamePlayerLeavingPacket;
import packets.battleship.BattleShipEndGamePacket;
import packets.battleship.BattleShipInGamePacket;
import packets.battleship.BattleShipStartConfirmPacket;
import packets.battleship.BattleShipStartInitGamePacket;
import packets.battleship.BattleShipStartPacket;
import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.morpion.MorpionEndGamePacket;
import packets.morpion.MorpionInGameConfirmPacket;
import packets.morpion.MorpionInGamePacket;
import packets.morpion.MorpionStartConfirmPacket;
import packets.morpion.MorpionStartPacket;
import packets.Packet;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * Main.java   :    main class of the server use to launch it
 * ---------------------------------------------------------------------------------------------
 */

public class Main {

    //port on wich the server will be host
    static int tcp = 23900, udp = 23901;

    //array containing wich player is waiting to play for a specific game
    public static int clientIDWaitingPerGame[] = {-1,-1};

    //index of each game in clientIDWaitingPerGame
    public final static int MORPION_INDEX = 0;
    public final static int BATTLESHIP_INDEX = 1;

    //constants use in the battleship game
    public final static int NB_CASE = 8;
    public final static int NB_SHIP = 3;

    //constants use when the server received a packet to check his validity
    public final static  int MIN_VERSION = 1;
    public final static  int CURRENT_VERSION = 1;

    //list of game and player
    static ArrayList<Game> listGames = new ArrayList<>();
    static PlayerList listPlayer = new PlayerList();


    public static HashMap<Integer, char[]> initializedGame = new HashMap<>();

    //this variable is use to know the index of a game, will be increment each time
    private static int currentGameId = 1;

    public static void main(String args[]) throws IOException {
        //creating a server object and registering needed packets
        Server server = new Server();

        server.getKryo().register(Packet.class, 100);
        server.getKryo().register(char[].class,110);
        server.getKryo().register(MiniGamePacket.class, 200);
        server.getKryo().register(LoginPacket.class, 300);
        server.getKryo().register(LoginConfirmPacket.class, 350);
        server.getKryo().register(MorpionStartPacket.class, 1001);
        server.getKryo().register(MorpionStartConfirmPacket.class, 1010);
        server.getKryo().register(MorpionInGamePacket.class,1020);
        server.getKryo().register(MorpionInGameConfirmPacket.class,1030);
        server.getKryo().register(MorpionEndGamePacket.class,1050);
        server.getKryo().register(GamePlayerLeavingPacket.class,1110);
        server.getKryo().register(BattleShipStartPacket.class, 2001);
        server.getKryo().register(BattleShipStartConfirmPacket.class, 2010);
        server.getKryo().register(BattleShipInGamePacket.class, 2020);
        server.getKryo().register(BattleShipStartInitGamePacket.class, 2030);
        server.getKryo().register(BattleShipEndGamePacket.class, 2040);

        //starting the server on specific port
        server.start();

        server.bind(tcp, udp);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {

                if (object instanceof Packet) {
                    Packet p = (Packet) object;
                    //check the validity of a packet when received
                    if (p.maj >= MIN_VERSION && p.maj <= CURRENT_VERSION) {
                        if (p instanceof MiniGamePacket) {
                            MiniGamePacket miniGamePacket = (MiniGamePacket) p;
                        }
                        if (p instanceof LoginPacket) {

                            //if someone new login in the server add him to the list of player using his
                            //connection id
                            LoginPacket lp = (LoginPacket) object;

                            listPlayer.add(new Player(connection.getID(), lp.namePlayer));

                            LoginConfirmPacket lcp = new LoginConfirmPacket();
                            lcp.namePlayer = lp.namePlayer;
                            server.sendToTCP(connection.getID(), lcp);
                        }
                        //BEGIN BATTLE SHIP
                        if (p instanceof BattleShipStartPacket) {
                            if (clientIDWaitingPerGame[BATTLESHIP_INDEX] == -1) {
                                clientIDWaitingPerGame[BATTLESHIP_INDEX] = connection.getID();
                            } else {
                                BattleShipStartConfirmPacket bsscp = new BattleShipStartConfirmPacket();
                                bsscp.idPlayer1 = clientIDWaitingPerGame[BATTLESHIP_INDEX];
                                bsscp.idPlayer2 = connection.getID();
                                clientIDWaitingPerGame[BATTLESHIP_INDEX] = -1;

                                bsscp.namePlayer1 = listPlayer.getPlayerById(bsscp.idPlayer1).getNamePlayer();
                                bsscp.namePlayer2 = listPlayer.getPlayerById(bsscp.idPlayer2).getNamePlayer();

                                listPlayer.getPlayerById(bsscp.idPlayer1).setPlaying(true);
                                listPlayer.getPlayerById(bsscp.idPlayer2).setPlaying(true);

                                Game battleShip = new Game(GameType.BATTLESHIP, bsscp.idPlayer1, bsscp.idPlayer2, currentGameId++);
                                battleShip.setCharPlayer1(bsscp.charPlayer1);
                                battleShip.setCharPlayer2(bsscp.charPlayer1);
                                listGames.add(battleShip);

                                listPlayer.getPlayerById(bsscp.idPlayer1).setCurrentGameId(battleShip.getId());
                                listPlayer.getPlayerById(bsscp.idPlayer2).setCurrentGameId(battleShip.getId());

                                bsscp.gameId = battleShip.getId();
                                server.sendToTCP(bsscp.idPlayer1, bsscp);
                                server.sendToTCP(bsscp.idPlayer2, bsscp);
                            }
                        } else if (p instanceof BattleShipStartInitGamePacket) {
                            BattleShipStartInitGamePacket bssigp = (BattleShipStartInitGamePacket) p;
                            if (initializedGame.containsKey(bssigp.idOpponent)) {
                                System.out.println("j'ai copain");
                                int idPlayer = bssigp.idPlayer;
                                int idOpponent = bssigp.idOpponent;
                                char[] tabPlayer = bssigp.tabGame;
                                char[] tabOpponent = initializedGame.get(bssigp.idOpponent);
                                if (bssigp.idPlayer > bssigp.idOpponent) {
                                    idPlayer = bssigp.idOpponent;
                                    idOpponent = bssigp.idPlayer;
                                    tabPlayer = initializedGame.get(bssigp.idOpponent);
                                    tabOpponent = bssigp.tabGame;
                                }

                                BattleShipInGamePacket bscig = new BattleShipInGamePacket();
                                bscig.currentPlayerId = idPlayer;
                                bscig.opponentPlayerId = idOpponent;
                                bscig.currentPlayerTab = tabPlayer;
                                bscig.currentPlayerTabTouched = new char[NB_CASE * NB_CASE];
                                bscig.opponentPlayerTabTouched = new char[NB_CASE * NB_CASE];
                                bscig.opponentPlayerTab = tabOpponent;
                                bscig.gameId = bssigp.gameId;

                                server.sendToTCP(bscig.currentPlayerId, bscig);
                                server.sendToTCP(bscig.opponentPlayerId, bscig);
                            } else {
                                initializedGame.put(bssigp.idPlayer, bssigp.tabGame);
                            }
                        } else if (p instanceof BattleShipInGamePacket) {
                            BattleShipInGamePacket bsigp = (BattleShipInGamePacket) p;
                            int counterTouched = 0;

                            for (int i = 0; i < bsigp.currentPlayerTab.length; i++) {
                                if (bsigp.currentPlayerTabTouched[i] == bsigp.opponentPlayerTab[i] && bsigp.currentPlayerTabTouched[i] != '\0') {
                                    counterTouched++;
                                }
                            }
                            //If the number of counterTouched is egual to the NB_SHIP this is the end of the game
                            if (counterTouched == NB_SHIP) {
                                BattleShipEndGamePacket bsegp = new BattleShipEndGamePacket();
                                bsegp.idWinner = bsigp.currentPlayerId;
                                server.sendToTCP(bsigp.currentPlayerId, bsegp);
                                server.sendToTCP(bsigp.opponentPlayerId, bsegp);
                                System.out.println("fin de la partie");

                                initializedGame.remove(bsigp.currentPlayerId);
                                initializedGame.remove(bsigp.opponentPlayerId);
                            } else {
                                //Read the packet and inverse the currentPlayer and the opponent
                                BattleShipInGamePacket sendBsigp = new BattleShipInGamePacket();
                                sendBsigp.currentPlayerId = bsigp.opponentPlayerId;
                                sendBsigp.opponentPlayerId = bsigp.currentPlayerId;
                                sendBsigp.currentPlayerTab = bsigp.opponentPlayerTab;
                                sendBsigp.opponentPlayerTab = bsigp.currentPlayerTab;
                                sendBsigp.currentPlayerTabTouched = bsigp.opponentPlayerTabTouched;
                                sendBsigp.opponentPlayerTabTouched = bsigp.currentPlayerTabTouched;
                                sendBsigp.gameId = bsigp.gameId;

                                server.sendToTCP(sendBsigp.currentPlayerId, sendBsigp);
                                server.sendToTCP(sendBsigp.opponentPlayerId, sendBsigp);
                            }

                        }
                        //BEGIN MORPION
                        if (p instanceof MorpionStartPacket) {
                            //if the is no one waiting the player, the player will wait
                            if (clientIDWaitingPerGame[MORPION_INDEX] == -1) {
                                clientIDWaitingPerGame[MORPION_INDEX] = connection.getID();
                            } else {
                                //if there is already someone waiting to play the game can start

                                //create a packet witch will be send to both player letting them know information about the game
                                MorpionStartConfirmPacket mscp = new MorpionStartConfirmPacket();

                                //synchronized the array to avoid concurrent problem
                                synchronized (clientIDWaitingPerGame)
                                {
                                    mscp.idPlayer1 = clientIDWaitingPerGame[MORPION_INDEX];
                                    mscp.player1Name = listPlayer.getPlayerById(mscp.idPlayer1).getNamePlayer();
                                    listPlayer.getPlayerById(mscp.idPlayer1).setPlaying(true);
                                    mscp.idPlayer2 = connection.getID();
                                    mscp.player2Name = listPlayer.getPlayerById(mscp.idPlayer2).getNamePlayer();
                                    listPlayer.getPlayerById(mscp.idPlayer2).setPlaying(true);

                                    //no one is waiting any more
                                    clientIDWaitingPerGame[MORPION_INDEX] = -1;
                                    System.out.println(mscp.idPlayer1 + " and " + mscp.idPlayer2 + " will play");

                                }

                                //create a new game and add it to the list
                                Game morpion = new Game(GameType.MORPION, mscp.idPlayer1, mscp.idPlayer2, currentGameId++);
                                morpion.setCharPlayer1(mscp.charPlayer1);
                                morpion.setCharPlayer2(mscp.charPlayer2);
                                listGames.add(morpion);

                                //update player info about what game they are currently playing
                                listPlayer.getPlayerById(mscp.idPlayer1).setCurrentGameId(morpion.getId());
                                listPlayer.getPlayerById(mscp.idPlayer2).setCurrentGameId(morpion.getId());

                                mscp.gameId = morpion.getId();


                                //send packet to players
                                server.sendToTCP(mscp.idPlayer1, mscp);
                                server.sendToTCP(mscp.idPlayer2, mscp);
                            }
                        }
                        if (p instanceof MorpionInGamePacket) {
                            MorpionInGamePacket migp = (MorpionInGamePacket) p;

                            GameChar winnerChar = new GameChar();

                            //check if the game is win or not
                            if (MorpionHandler.getInstance().isGameOver(migp.tabGame, winnerChar)) {
                                //if someone has won the game or if it is a draw

                                //crate a packet telling the player that the game is over
                                MorpionEndGamePacket megp = new MorpionEndGamePacket();

                                //use the winner char to find the winner id
                                if (winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer1()) {
                                    //player 1 win
                                    megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer1();
                                } else if (winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer2()) {
                                    //player 2 win
                                    megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer2();
                                } else {
                                    megp.winnerId = -1;
                                }

                                megp.gameId = migp.gameId;
                                megp.tabGame = migp.tabGame;

                                //players are no longer playing
                                listPlayer.getPlayerById(migp.currentPlayerID).setPlaying(false);
                                listPlayer.getPlayerById(migp.opponentPlayerID).setPlaying(false);

                                //send them the packet
                                server.sendToTCP(migp.currentPlayerID, megp);
                                server.sendToTCP(migp.opponentPlayerID, megp);

                            } else {

                                //if the game is not win create a new packet and send it to both player
                                MorpionInGameConfirmPacket migcp = new MorpionInGameConfirmPacket();
                                migcp.tabGame = migp.tabGame;
                                migcp.currentPlayerID = migp.opponentPlayerID;

                                server.sendToTCP(migp.currentPlayerID, migcp);
                                server.sendToTCP(migp.opponentPlayerID, migcp);
                            }
                        } //END MORPION
                        if (p instanceof GamePlayerLeavingPacket) {
                            //if a player is leaving a game using going back to the main menu the server will go here
                            GamePlayerLeavingPacket mpl = (GamePlayerLeavingPacket) p;

                            System.out.println("Player leaving");

                            //check if he was waiting to play if it's the case remove him from the array
                            for (int i = 0; i < clientIDWaitingPerGame.length; i++) {
                                if (clientIDWaitingPerGame[i] == mpl.playerid) {
                                    clientIDWaitingPerGame[i] = -1;
                                }
                            }

                            //remove the player entry in the game board hashmap use by the battleship game
                            if(listPlayer.exist(connection.getID()) && initializedGame.containsKey(connection.getID())){

                                int player1 = connection.getID();
                                int player2;
                                Game currentGame = selectGameFromId(listPlayer.getPlayerById(connection.getID()).getCurrentGameId());

                                if(player1 != currentGame.getIdPlayer1()){
                                    player2 = currentGame.getIdPlayer1();
                                }
                                else{
                                    player2 = currentGame.getIdPlayer2();
                                }

                                initializedGame.remove(player1);
                                initializedGame.remove(player2);

                            }

                            //if the player was in a game
                            if (listPlayer.getPlayerById(mpl.playerid).isPlaying()) {
                                //found the player's game
                                Game game = selectGameFromId(listPlayer.getPlayerById(mpl.playerid).getCurrentGameId());
                                //found his opponent and send him the playerleaving packet
                                if (game.getIdPlayer1() != mpl.playerid) {
                                    server.sendToTCP(game.getIdPlayer1(), mpl);
                                } else {
                                    server.sendToTCP(game.getIdPlayer2(), mpl);
                                }

                                //both player are no longer playing
                                listPlayer.getPlayerById(game.getIdPlayer1()).setPlaying(false);
                                listPlayer.getPlayerById(game.getIdPlayer2()).setPlaying(false);
                            }

                        }

                    }
                }
            }

            @Override
            public void disconnected(Connection connection) {
                //if a client disconnect from the server

                //check if he was waiting for a game, if hw was remove him from the array
                for(int i=0;i<clientIDWaitingPerGame.length;i++){
                    if(clientIDWaitingPerGame[i] == connection.getID()){
                        clientIDWaitingPerGame[i] = -1;
                    }
                }

                //remove the player entry in the game board hashmap use by the battleship game
                if(listPlayer.exist(connection.getID()) && initializedGame.containsKey(connection.getID())){

                    int player1 = connection.getID();
                    int player2;
                    Game currentGame = selectGameFromId(listPlayer.getPlayerById(connection.getID()).getCurrentGameId());

                    if(player1 != currentGame.getIdPlayer1()){
                        player2 = currentGame.getIdPlayer1();
                    }
                    else{
                        player2 = currentGame.getIdPlayer2();
                    }

                    initializedGame.remove(player1);
                    initializedGame.remove(player2);

                }

                //check if the player was playing, if he notify his opponent
                if(listPlayer.exist(connection.getID()) && listPlayer.getPlayerById(connection.getID()).isPlaying()){
                    Game game = selectGameFromId(listPlayer.getPlayerById(connection.getID()).getCurrentGameId());

                    GamePlayerLeavingPacket gplp = new GamePlayerLeavingPacket();

                    gplp.playerid = connection.getID();
                    gplp.playerName = listPlayer.getPlayerById(connection.getID()).getNamePlayer();

                    if(game.getIdPlayer1() != connection.getID()){
                        server.sendToTCP(game.getIdPlayer1(),gplp);
                    }
                    else{
                        server.sendToTCP(game.getIdPlayer2(),gplp);
                    }
                }

            }

            @Override
            public void connected(Connection connection) {

            }

        });

    }


    /**
     * return a game from the list of game based of his id
     * @param id id of the game to get
     * @return game
     */
    public static Game selectGameFromId(int id){
        for(Game g : listGames){
            if(g.getId() == id){
                return g;
            }
        }
        return null;
    }

}
