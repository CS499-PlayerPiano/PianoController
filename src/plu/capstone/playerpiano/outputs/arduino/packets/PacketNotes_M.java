package plu.capstone.playerpiano.outputs.arduino.packets;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;

/*
Mutiple note packet format:
    M - Packet ID
    Number of notes
    Velocity
    Array of notes:
        Key

  isValid is set to false if the notes have different velocities
*/
public class PacketNotes_M extends PacketNotes {

    private static final Logger logger = new Logger(PacketNotes_M.class);

    private boolean isValid = true;

    public PacketNotes_M(List<NoteEvent> notes, Map<Integer, Integer> noteMapping, int velocityMappingMin, int velocityMappingMax, boolean ignoreVelocity) {
        super(notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);
    }

    @Override
    protected void writeBytes() {

        final NoteEvent firstNote = notes.get(0);

        int velocity = firstNote.getVelocity();
        for(NoteEvent note : notes) {
            if(note.getVelocity() != velocity) {
                isValid = false;
                return;
            }
        }

        allocate(3 + notes.size());
        write('M');
        write(notes.size());

        if(firstNote.isNoteOn()) {
            velocity = mapVelocity(velocity);
        }
        else {
            velocity = 0;
        }

        write(velocity);

        for(NoteEvent note : notes) {
            Integer keyIndex = noteMapping.get(note.getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + note.getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }
            write(keyIndex);
        }
    }

    public boolean isValid() {
        processPacket();
        return isValid;
    }
}
