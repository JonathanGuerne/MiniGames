package packets;

/**
 * @author Jonathan Guerne
 * @date 22 juin 2015
 */
public class Packet {
    public int maj = 1;
    public long creationTime;

    public Packet() {
        creationTime = System.currentTimeMillis();
    }
}
