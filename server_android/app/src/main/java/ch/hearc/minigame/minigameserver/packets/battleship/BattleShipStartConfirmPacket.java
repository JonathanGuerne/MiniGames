package ch.hearc.minigame.minigameserver.packets.battleship;

import ch.hearc.minigame.minigameserver.packets.Packet;

/**
 * Created by anthony.gillioz on 03.07.2017.
 */

public class BattleShipStartConfirmPacket extends Packet
{
    public int gameId;
    public int idPlayer1;
    public char charPlayer1 = 'x';
    public int idPlayer2;
    public char charPlayer2 = 'o';

}
