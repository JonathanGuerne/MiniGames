package serverprogram;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import packets.MorpionPlayerLeaving;
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

/**
 * @author Jonathan Guerne
 */
public class Main {

    static int tcp = 23900, udp = 23901;

    public static HashMap<Integer, Connection> clients = new HashMap<Integer, Connection>();
    
    public static int clientIDWaitingPerGame[] = {-1,-1};

    public final static int MORPION_INDEX = 0;
    public final static int BATTLESHIP_INDEX = 1;

    public final static int NB_CASE = 8;
    public final static int NB_SHIP = 3;

    static ArrayList<Game> listGames = new ArrayList<>();
    static PlayerList listPlayer = new PlayerList();

    public static HashMap<Integer, char[]> initializedGame = new HashMap<>();

    private static int currentGameId = 1;

    public static void main(String args[]) throws IOException {
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

        server.start();

        server.bind(tcp, udp);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {

                if (object instanceof Packet) {
                    Packet p = (Packet) object;
                    if (p instanceof MiniGamePacket)
                    {
                        MiniGamePacket miniGamePacket = (MiniGamePacket) p;
                    }
                    if (p instanceof LoginPacket) {

                        LoginPacket lp = (LoginPacket) object;

                        listPlayer.add(new Player(connection.getID(),lp.namePlayer));

                        LoginConfirmPacket lcp = new LoginConfirmPacket();
                        lcp.namePlayer =lp.namePlayer;
                        server.sendToTCP(connection.getID(), lcp);
                    }
                    //BEGIN BATTLE SHIP
                    if(p instanceof BattleShipStartPacket)
                    {
                        if(clientIDWaitingPerGame[BATTLESHIP_INDEX] == -1)
                        {
                            clientIDWaitingPerGame[BATTLESHIP_INDEX] = connection.getID();
                        }else
                        {
                            BattleShipStartConfirmPacket bsscp = new BattleShipStartConfirmPacket();
                            bsscp.idPlayer1 = clientIDWaitingPerGame[BATTLESHIP_INDEX];
                            bsscp.idPlayer2 = connection.getID();
                            clientIDWaitingPerGame[BATTLESHIP_INDEX] = -1;

                            Game battleShip = new Game(GameType.BATTLESHIP, bsscp.idPlayer1, bsscp.idPlayer2, currentGameId++);
                            battleShip.setCharPlayer1(bsscp.charPlayer1);
                            battleShip.setCharPlayer2(bsscp.charPlayer1);
                            listGames.add(battleShip);

                            bsscp.gameId = battleShip.getId();
                            server.sendToTCP(bsscp.idPlayer1, bsscp);
                            server.sendToTCP(bsscp.idPlayer2, bsscp);
                        }
                    }else if(p instanceof BattleShipStartInitGamePacket)
                    {
                        BattleShipStartInitGamePacket bssigp = (BattleShipStartInitGamePacket) p;
                        if(initializedGame.containsKey(bssigp.idOpponent))
                        {
                            System.out.println("j'ai copain");
                            int idPlayer = bssigp.idPlayer;
                            int idOpponent = bssigp.idOpponent;
                            char[] tabPlayer = bssigp.tabGame;
                            char[] tabOpponent = initializedGame.get(bssigp.idOpponent);
                            if(bssigp.idPlayer > bssigp.idOpponent)
                            {
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
                        }else
                        {
                            System.out.println("Je suis tout seul " + bssigp.idPlayer + " " + bssigp.idOpponent);
                            initializedGame.put(bssigp.idPlayer, bssigp.tabGame);
                        }
                    }else if(p instanceof BattleShipInGamePacket)
                    {
                        BattleShipInGamePacket bsigp = (BattleShipInGamePacket) p;
                        int counterTouched = 0;

                        for(int i = 0; i < bsigp.currentPlayerTab.length; i++)
                        {
                            if(bsigp.currentPlayerTabTouched[i] == bsigp.opponentPlayerTab[i] && bsigp.currentPlayerTabTouched[i] != '\0')
                            {
                                counterTouched++;
                            }
                        }
                        //If the number of counterTouched is egual to the NB_SHIP this is the end of the game
                        if(counterTouched == NB_SHIP)
                        {
                            BattleShipEndGamePacket bsegp = new BattleShipEndGamePacket();
                            bsegp.idWinner = bsigp.currentPlayerId;
                            server.sendToTCP(bsigp.currentPlayerId, bsegp);
                            server.sendToTCP(bsigp.opponentPlayerId, bsegp);
                            System.out.println("fin de la partie");
                        }else
                        {
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
                       if(clientIDWaitingPerGame[MORPION_INDEX] == -1){
                           clientIDWaitingPerGame[MORPION_INDEX] = connection.getID();
                       }
                       else{
                           MorpionStartConfirmPacket mscp = new MorpionStartConfirmPacket();
                           mscp.idPlayer1 = clientIDWaitingPerGame[MORPION_INDEX];
                           mscp.player1Name = listPlayer.getPlayerById(mscp.idPlayer1).getNamePlayer();
                           listPlayer.getPlayerById(mscp.idPlayer1).setPlaying(true);
                           mscp.idPlayer2 = connection.getID();
                           mscp.player2Name = listPlayer.getPlayerById(mscp.idPlayer2).getNamePlayer();
                           listPlayer.getPlayerById(mscp.idPlayer2).setPlaying(true);


                           clientIDWaitingPerGame[MORPION_INDEX] = -1;
                           System.out.println(mscp.idPlayer1+" and "+mscp.idPlayer2+" will play");

                           Game morpion =new Game(GameType.MORPION,mscp.idPlayer1,mscp.idPlayer2,currentGameId++);
                           morpion.setCharPlayer1(mscp.charPlayer1);
                           morpion.setCharPlayer2(mscp.charPlayer2);
                           listGames.add(morpion);

                           listPlayer.getPlayerById(mscp.idPlayer1).setCurrentGameId(morpion.getId());
                           listPlayer.getPlayerById(mscp.idPlayer2).setCurrentGameId(morpion.getId());

                           mscp.gameId = morpion.getId();

                           server.sendToTCP(mscp.idPlayer1, mscp);
                           server.sendToTCP(mscp.idPlayer2, mscp);
                       }
                    }
                    if(p instanceof MorpionInGamePacket){
                        MorpionInGamePacket migp = (MorpionInGamePacket) p;

                        GameChar winnerChar = new GameChar();

                        if(MorpionHandler.getInstance().isGameOver(migp.tabGame,winnerChar)){

                            MorpionEndGamePacket megp = new MorpionEndGamePacket();

                            if(winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer1()){
                                //player 1 win
                                megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer1();
                            }
                            else if(winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer2()){
                                //player 2 win
                                megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer2();
                            }
                            else{
                                megp.winnerId = -1;
                            }

                            megp.gameId = migp.gameId;
                            megp.tabGame = migp.tabGame;

                            listPlayer.getPlayerById(migp.currentPlayerID).setPlaying(false);
                            listPlayer.getPlayerById(migp.opponentPlayerID).setPlaying(false);

                            server.sendToTCP(migp.currentPlayerID,megp);
                            server.sendToTCP(migp.opponentPlayerID,megp);

                        }
                        else {

                            MorpionInGameConfirmPacket migcp = new MorpionInGameConfirmPacket();
                            migcp.tabGame = migp.tabGame;
                            migcp.currentPlayerID = migp.opponentPlayerID;

                            server.sendToTCP(migp.currentPlayerID, migcp);
                            server.sendToTCP(migp.opponentPlayerID, migcp);
                        }
                    }
                    if(p instanceof GamePlayerLeavingPacket){
                        GamePlayerLeavingPacket mpl = (GamePlayerLeavingPacket) p;

                        System.out.println("Player leaving");

                        if(clientIDWaitingPerGame[MORPION_INEX] == mpl.playerid){
                            clientIDWaitingPerGame[MORPION_INEX] = -1;
                        }
                        else if(listPlayer.getPlayerById(mpl.playerid).isPlaying()){
                            Game game = selectGameFromId(listPlayer.getPlayerById(mpl.playerid).getCurrentGameId());
                            if(game.getIdPlayer1() != mpl.playerid){
                                server.sendToTCP(game.getIdPlayer1(),mpl);
                            }
                            else{
                                server.sendToTCP(game.getIdPlayer2(),mpl);
                            }

                            listPlayer.getPlayerById(game.getIdPlayer1()).setPlaying(false);
                            listPlayer.getPlayerById(game.getIdPlayer2()).setPlaying(false);
                        }

                    }
                    //END MORPION
                }
            }

            @Override
            public void disconnected(Connection connection) {
                for(int i=0;i<clientIDWaitingPerGame.length;i++){
                    if(clientIDWaitingPerGame[i] == connection.getID()){
                        clientIDWaitingPerGame[i] = -1;
                    }
                }

                if(listPlayer.getPlayerById(connection.getID()).isPlaying()){
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


    public static Game selectGameFromId(int id){
        for(Game g : listGames){
            if(g.getId() == id){
                return g;
            }
        }
        return null;
    }

}
