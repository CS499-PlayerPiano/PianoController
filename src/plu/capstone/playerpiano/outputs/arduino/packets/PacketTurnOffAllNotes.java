package plu.capstone.playerpiano.outputs.arduino.packets;

public class PacketTurnOffAllNotes extends Packet {

        @Override
        public void writeBytes() {
            allocate(1);
            write('O');
        }
}
