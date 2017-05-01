package ch.arc.ete;

import com.esotericsoftware.kryonet.Client;

import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
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

        return client;
    }

}
