/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packets;

/**
 *
 * @author jonathan.guerne
 */
public class MorpionInGameConfirmPacket extends Packet{
    public char[] tabGame;
    public int currentPlayerID;
}
