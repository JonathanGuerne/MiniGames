package serverprogram;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
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

    public static void main(String args[]) throws IOException {
       
        
        
        Server server = new Server();

        server.getKryo().register(Packet.class, 100);
        server.getKryo().register(MiniGamePacket.class, 200);
        server.getKryo().register(LoginPacket.class, 300);
        server.getKryo().register(LoginConfirmPacket.class, 350);
        server.getKryo().register(MorpionStartPacket.class, 1001);
        server.getKryo().register(MorpionStartConfirmPacket.class, 1010);
        
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
                    if (p instanceof MorpionStartPacket) {
                       if(clientIDWaitingPerGame[0] == -1){
                           clientIDWaitingPerGame[0] = connection.getID();
                           System.out.println("change user waiting id to "+connection.getID());
                       }
                       else{
                           MorpionStartConfirmPacket mscp = new MorpionStartConfirmPacket();
                           mscp.idPlayer1 = clientIDWaitingPerGame[0];
                           mscp.idPlaer2 = connection.getID();
                           clientIDWaitingPerGame[0] = -1;
                           System.out.println(mscp.idPlayer1+" and "+mscp.idPlaer2+" will play");
                           server.sendToTCP(mscp.idPlayer1, mscp);
                           server.sendToTCP(mscp.idPlaer2,mscp);
                       }
                    }
//                        if (object instanceof Packet1Connect) {
//                            Packet1Connect p1 = (Packet1Connect) object;
//                            clients.put(connection.getID(), connection);
//
//                            Packet1_5ConnectConfirm p1_5 = new Packet1_5ConnectConfirm();
//                            p1_5.ID = connection.getID();
//
//                            server.sendToTCP(connection.getID(), p1_5);
//                            
//                            
//                            Packet2ClientConnected p2 = new Packet2ClientConnected();
//                            p2.clientName = p1.username;
//                            server.sendToAllExceptTCP(connection.getID(), p2);
//                            Packet5ListUsers p5 = new Packet5ListUsers();
//                            for (Entry<Integer, Connection> entry : clients.entrySet()) {
//                                int key = entry.getKey();
//                                p5.users.add(clients_name.get(key));
//                            }
//
//                            server.sendToAllTCP(p5);
//                        } else if (object instanceof Packet3ClientDisconnect) {
//                            Packet3ClientDisconnect p3 = (Packet3ClientDisconnect) object;
//                            clients.remove(p3.ClientName);
//                            server.sendToAllExceptTCP(clients.get(p3.ClientName).getID(), p3);
//                        } else if (object instanceof Packet4Chat) {
//                            Packet4Chat p4 = (Packet4Chat) object;
//                            server.sendToAllTCP(p4);
//                        }
//                    } else {
////                        Packet6Error p6 = new Packet6Error();
////                        p6.erreorMessage = "Mauvaise mise à jour";
////                        server.sendToTCP(connection.getID(), p6);

                }
            }

            @Override
            public void disconnected(Connection connection) {

                System.out.println("Client Leaving :ID : " + connection.getID());

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

}
