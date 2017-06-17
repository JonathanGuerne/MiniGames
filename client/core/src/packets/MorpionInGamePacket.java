package packets;

/**
 * Created by jonathan.guerne on 07.06.2017.
 */

public class MorpionInGamePacket extends Packet {
    public int currentPlayerID;
    public int opponentPlayerID;
    public char currentPlayerChar;
    public char[] tabGame;
}
