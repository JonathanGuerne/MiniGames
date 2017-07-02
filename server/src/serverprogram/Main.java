package serverprogram;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.MorpionEndGamePacket;
import packets.MorpionInGameConfirmPacket;
import packets.MorpionInGamePacket;
import packets.MorpionStartConfirmPacket;
import packets.MorpionStartPacket;
import packets.Packet;

/**
 * @author Jonathan Guerne
 */
public class Main {

    static int tcp = 23900, udp = 23901;

    public static HashMap<Integer, Connection> clients = new HashMap<Integer, Connection>();
    
    public static int clientIDWaitingPerGame[] = {-1,-1};

    static ArrayList<Game> listGames = new ArrayList<>();

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

        server.start();

        server.bind(tcp, udp);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {

                if (object instanceof Packet) {
                    System.out.println("recu un packet");
                    Packet p = (Packet) object;
                    if (p instanceof MiniGamePacket) {
                        MiniGamePacket miniGamePacket = (MiniGamePacket) p;
                        System.out.println("Client à envoyé : " + miniGamePacket.answer);

                    }
                    if (p instanceof LoginPacket) {
                        LoginConfirmPacket lcp = new LoginConfirmPacket();
                        lcp.msg = "connected";
                        server.sendToTCP(connection.getID(), lcp);
                    }
                    //BEGIN MORPION
                    if (p instanceof MorpionStartPacket) {
                       if(clientIDWaitingPerGame[0] == -1){
                           clientIDWaitingPerGame[0] = connection.getID();
                           System.out.println("change user waiting id to "+connection.getID());
                       }
                       else{
                           MorpionStartConfirmPacket mscp = new MorpionStartConfirmPacket();
                           mscp.idPlayer1 = clientIDWaitingPerGame[0];
                           mscp.idPlayer2 = connection.getID();
                           clientIDWaitingPerGame[0] = -1;
                           System.out.println(mscp.idPlayer1+" and "+mscp.idPlayer2+" will play");

                           Game morpion =new Game(GameType.MORPION,mscp.idPlayer1,mscp.idPlayer2,currentGameId++);
                           morpion.setCharPlayer1(mscp.charPlayer1);
                           morpion.setCharPlayer2(mscp.charPlayer2);
                           listGames.add(morpion);

                           mscp.gameId = morpion.getId();

                           server.sendToTCP(mscp.idPlayer1, mscp);
                           server.sendToTCP(mscp.idPlayer2,mscp);
                       }
                    }
                    if(p instanceof MorpionInGamePacket){
                        MorpionInGamePacket migp = (MorpionInGamePacket) p;

                        GameChar winnerChar = new GameChar();


                        if(MorpionHandler.getInstance().isGameOver(migp.tabGame,winnerChar)){

                            MorpionEndGamePacket megp = new MorpionEndGamePacket();

                            if(winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer1()){
                                //player 1 win
                                System.out.println("PLAYER 1 WIN");
                                megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer1();
                            }
                            else if(winnerChar.getInfoChar() == selectGameFromId(migp.gameId).getCharPlayer2()){
                                //player 2 win
                                System.out.println("PLAYER 2 WIN");
                                megp.winnerId = selectGameFromId(migp.gameId).getIdPlayer2();
                            }
                            else{
                                System.out.println("DRAW");
                                megp.winnerId = -1;
                            }

                            System.out.println("Winner id = "+megp.winnerId);

                            megp.gameId = migp.gameId;
                            megp.tabGame = migp.tabGame;

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
                    //END MORPION
                }
            }

            @Override
            public void disconnected(Connection connection) {

                System.out.println("Client Leaving :ID : " + connection.getID());
                for(int i=0;i<clientIDWaitingPerGame.length;i++){
                    if(clientIDWaitingPerGame[i] == connection.getID()){
                        clientIDWaitingPerGame[i] = -1;
                    }
                }

            }

            @Override
            public void connected(Connection connection) {
                System.out.println(" new Client : ID : " + connection.getID());
                MiniGamePacket mp = new MiniGamePacket();
                mp.answer = 42;

                server.sendToTCP(connection.getID(), mp);
            }

        });

        MiniGamePacket mp = new MiniGamePacket();
        mp.answer = 42;

        System.out.println("Server ready");

        while (true) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            //System.out.println("send answer");
            server.sendToAllTCP(mp);
        }
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
