package ch.arc.ete;

import com.esotericsoftware.kryonet.Client;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.MorpionStartConfirmPacket;
import packets.MorpionStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class ClientBuilder {

    public Client build(Client client){

        client.getKryo().register(Packet.class, 100);
        client.getKryo().register(MiniGamePacket.class, 200);
        client.getKryo().register(LoginPacket.class,300);
        client.getKryo().register(LoginConfirmPacket.class,350);
        client.getKryo().register(MorpionStartPacket.class,1001);
        client.getKryo().register(MorpionStartConfirmPacket.class,1010);

        return client;
    }

}
