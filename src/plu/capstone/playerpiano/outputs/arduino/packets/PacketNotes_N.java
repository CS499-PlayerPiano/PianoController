package plu.capstone.playerpiano.outputs.arduino.packets;

import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;

/*
Generic send notes packet
N - Packet ID
Number of notes
Array of notes:
    Key
    Velocity
   */
public class PacketNotes_N extends PacketNotes {

    private static final Logger logger = new Logger(PacketNotes_N.class);

    public PacketNotes_N(List<NoteEvent> notes, Map<Integer, Integer> noteMapping, int velocityMappingMin, int velocityMappingMax, boolean ignoreVelocity) {
        super( notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);
    }

    @Override
    protected void writeBytes() {

        allocate(2 + (notes.size() * 2));

        write('N');
        write(notes.size());

        for(NoteEvent note : notes) {
            Integer keyIndex = noteMapping.get(note.getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + note.getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }
            write(keyIndex);

            //If the note is on, the velocity isn't 0. If the note is off, the velocity is 0
            byte velocity = 0;

            if(note.isNoteOn()){
                velocity = mapVelocity(note.getVelocity());
            }
            else {
                velocity = 0;
            }

            write(velocity);
        }
    }
}
