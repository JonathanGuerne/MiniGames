package packets;

/**
 * Created by jonathan.guerne on 31.05.2017.
 */

public class MorpionStartConfirmPacket extends Packet {
    public int gameId;
    public int idPlayer1;
    public char charPlayer1 = 'x';
    public int idPlayer2;
    public char charPlayer2 = 'o';
}
