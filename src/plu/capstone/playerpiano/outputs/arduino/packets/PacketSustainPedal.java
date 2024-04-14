package plu.capstone.playerpiano.outputs.arduino.packets;

import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

public class PacketSustainPedal extends Packet {

    private final boolean pedalDown;

    public PacketSustainPedal(boolean pedalDown) {
        super(2);
        this.pedalDown = pedalDown;
    }


    @Override
    public void writeBytes() {
        write('S');
        write(pedalDown ? 255 : 0);
    }
}
