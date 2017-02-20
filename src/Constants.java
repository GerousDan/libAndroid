package xavier.btkom;

/**
 * Created by Xavier on 19/02/2017.
 */

public class Constants {

    // GLOBAL TRANSFER INFORMATIONS
    public final int SIZE_TX = 1000;
    public final int SIZE_RX = 255;
    public final int SIZE_HEADER = 46;
    public final int SIZE_DATA_TX = (SIZE_RX - SIZE_HEADER);

    // TYPES
    public final int RQT = 1;
    public final int ASW = 2;
    public final int ACK = 4;
    public final int HED = 8;
    public final int FUL = 16;
    public final int ERR = 32;
    public final int END = 64;
    public final int SYN = 128;

    // POSITIONS IN HEADER
    public final int TIME_POS = 0;
    public final int TYPE_POS = TIME_POS + 16;
    public final int ID_POS = TYPE_POS + 1;
    public final int SENDER_POS = ID_POS + 4;
    public final int MESSAGE_LENGTH_POS = SENDER_POS + 10;
    public final int PACKET_LENGTH_POS = MESSAGE_LENGTH_POS + 4;
    public final int PART_POS = PACKET_LENGTH_POS + 4;
    public final int NUMBER_PART_POS = PART_POS + 2;

}
