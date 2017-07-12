package ch.arc.ete;

import com.esotericsoftware.kryonet.Client;

import packets.BattleShip.BattleShipEndGamePacket;
import packets.BattleShip.BattleShipInGamePacket;
import packets.BattleShip.BattleShipStartConfirmPacket;
import packets.BattleShip.BattleShipStartInitGamePacket;
import packets.BattleShip.BattleShipStartPacket;
import packets.LoginConfirmPacket;
import packets.LoginPacket;
import packets.MiniGamePacket;
import packets.morpion.MorpionEndGamePacket;
import packets.morpion.MorpionInGameConfirmPacket;
import packets.morpion.MorpionInGamePacket;
import packets.GamePlayerLeavingPacket;
import packets.morpion.MorpionStartConfirmPacket;
import packets.morpion.MorpionStartPacket;
import packets.Packet;

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class ClientBuilder {

    public Client build(Client client){

        client.getKryo().register(Packet.class, 100);
        client.getKryo().register(char[].class,110);
        client.getKryo().register(MiniGamePacket.class, 200);
        client.getKryo().register(LoginPacket.class,300);
        client.getKryo().register(LoginConfirmPacket.class,350);
        client.getKryo().register(MorpionStartPacket.class,1001);
        client.getKryo().register(MorpionStartConfirmPacket.class,1010);
        client.getKryo().register(MorpionInGamePacket.class,1020);
        client.getKryo().register(MorpionInGameConfirmPacket.class,1030);
        client.getKryo().register(MorpionEndGamePacket.class,1050);
        client.getKryo().register(GamePlayerLeavingPacket.class,1110);
        client.getKryo().register(BattleShipStartPacket.class, 2001);
        client.getKryo().register(BattleShipStartConfirmPacket.class, 2010);
        client.getKryo().register(BattleShipInGamePacket.class, 2020);
        client.getKryo().register(BattleShipStartInitGamePacket.class, 2030);
        client.getKryo().register(BattleShipEndGamePacket.class, 2040);

        return client;
    }

}
