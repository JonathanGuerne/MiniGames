package packets.BattleShip;

import packets.Packet;

/**
 * Created by anthony.gillioz on 04.07.2017.
 */

public class BattleShipStartInitGamePacket extends Packet
{
    public char[] tabGame;
    public int idPlayer;
    public int idOpponent;
    public int gameId;
}
