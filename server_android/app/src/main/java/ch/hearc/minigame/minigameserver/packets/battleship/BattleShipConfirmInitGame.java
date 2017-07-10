package ch.hearc.minigame.minigameserver.packets.battleship;

import ch.hearc.minigame.minigameserver.packets.Packet;

/**
 * Created by anthony.gillioz on 05.07.2017.
 */

public class BattleShipConfirmInitGame extends Packet
{
    public int currentPlayerId;
    public int opponentPlayerId;
    public char[] currentPlayerTab;
    public char[] opponentPlayerTab;
    public int gameId;
}
