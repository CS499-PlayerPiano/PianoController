package plu.capstone.playerpiano.outputs.arduino.packets;

public class PacketResetPower extends Packet {

    @Override
    protected void writeBytes() {
        allocate(1);
        write('P');
    }

}
