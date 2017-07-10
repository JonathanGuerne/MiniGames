package ch.hearc.minigame.minigameserver.serverprogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hearc.minigame.minigameserver.R;
import ch.hearc.minigame.minigameserver.packets.battleship.BattleShipConfirmInitGame;
import ch.hearc.minigame.minigameserver.packets.battleship.BattleShipInGamePacket;
import ch.hearc.minigame.minigameserver.packets.battleship.BattleShipStartConfirmPacket;
import ch.hearc.minigame.minigameserver.packets.battleship.BattleShipStartInitGamePacket;
import ch.hearc.minigame.minigameserver.packets.battleship.BattleShipStartPacket;
import ch.hearc.minigame.minigameserver.packets.LoginConfirmPacket;
import ch.hearc.minigame.minigameserver.packets.LoginPacket;
import ch.hearc.minigame.minigameserver.packets.MiniGamePacket;
import ch.hearc.minigame.minigameserver.packets.MorpionEndGamePacket;
import ch.hearc.minigame.minigameserver.packets.MorpionInGameConfirmPacket;
import ch.hearc.minigame.minigameserver.packets.MorpionInGamePacket;
import ch.hearc.minigame.minigameserver.packets.MorpionStartConfirmPacket;
import ch.hearc.minigame.minigameserver.packets.MorpionStartPacket;
import ch.hearc.minigame.minigameserver.packets.Packet;

/**
 * @author Jonathan Guerne
 */
public class Main extends AppCompatActivity{

    static int tcp = 23900, udp = 23901;

    public static HashMap<Integer, Connection> clients = new HashMap<Integer, Connection>();
    
    public static int clientIDWaitingPerGame[] = {-1,-1};

    static ArrayList<Game> listGames = new ArrayList<>();

    public static HashMap<Integer, char[]> initializedGame = new HashMap<>();

    private static int currentGameId = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final TextView textInfoServer = (TextView) findViewById(R.id.textAreaInfoServer);

        final Button button = (Button) findViewById(R.id.btnStartServer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setEnabled(false);
                textInfoServer.setText("test");
                try {
                    launch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public  void launch() throws IOException {


        
        final Server server = new Server();

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
        server.getKryo().register(BattleShipStartPacket.class, 2001);
        server.getKryo().register(BattleShipStartConfirmPacket.class, 2010);
        server.getKryo().register(BattleShipInGamePacket.class, 2020);

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
                    //BEGIN BATTLE SHIP
                    if(p instanceof BattleShipStartPacket)
                    {
                        System.out.println("Bonjour, j'ai un packet pour vous. ID : " + connection.getID());
                        if(clientIDWaitingPerGame[0] == -1)
                        {
                            clientIDWaitingPerGame[0] = connection.getID();
                        }else
                        {
                            BattleShipStartConfirmPacket bsscp = new BattleShipStartConfirmPacket();
                            bsscp.idPlayer1 = clientIDWaitingPerGame[0];
                            bsscp.idPlayer2 = connection.getID();
                            clientIDWaitingPerGame[0] = -1;

                            Game battleShip = new Game(GameType.BATTLESHIP, bsscp.idPlayer1, bsscp.idPlayer2, currentGameId++);
                            battleShip.setCharPlayer1(bsscp.charPlayer1);
                            battleShip.setCharPlayer2(bsscp.charPlayer2);
                            listGames.add(battleShip);

                            bsscp.gameId = battleShip.getId();
                            server.sendToTCP(bsscp.idPlayer1, bsscp);
                            server.sendToTCP(bsscp.idPlayer2, bsscp);
                        }
                    }else if(p instanceof BattleShipStartInitGamePacket)
                    {
                        System.out.println("Je commence la synchro");
                        BattleShipStartInitGamePacket bssigp = (BattleShipStartInitGamePacket) p;
                        if(initializedGame.containsKey(bssigp.idOpponent))
                        {
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

                            BattleShipConfirmInitGame bscig = new BattleShipConfirmInitGame();
                            bscig.currentPlayerId = idPlayer;
                            bscig.opponentPlayerId = idOpponent;
                            bscig.currentPlayerTab = tabPlayer;
                            bscig.opponentPlayerTab = tabOpponent;
                            bscig.gameId = bssigp.gameId;

                            server.sendToTCP(bscig.currentPlayerId, bscig);
                            server.sendToTCP(bscig.opponentPlayerId, bscig);
                        }
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
