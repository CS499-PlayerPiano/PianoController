package plu.capstone.playerpiano.outputs.arduino.packets;

public class PacketTurnOffAllNotes extends Packet {

        public PacketTurnOffAllNotes() {
            super(1);
        }

        @Override
        public void writeBytes() {
            write('O');
        }
}
