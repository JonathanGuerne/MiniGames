package packets.BattleShip;

import packets.Packet;

/**
 * Created by anthony.gillioz on 04.07.2017.
 */

public class BattleShipInGamePacket extends Packet
{
    public int currentPlayerId;
    public int opponentPlayerId;
    public char[] currentPlayerTab;
    public char[] currentPlayerTabTouched;
    public char[] opponentPlayerTab;
    public char[] opponentPlayerTabTouched;
    public int gameId;
}
