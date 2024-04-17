package plu.capstone.playerpiano.outputs.arduino.packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;

/*
Note slam packet
B - Packet ID
Number of batches
    Array of batches:
        contiguous
        startingKey
        Velocity
    */
public class PacketNotes_B extends PacketNotes {

    private static final Logger logger = new Logger(PacketNotes_B.class);

    public PacketNotes_B(List<NoteEvent> notes, Map<Integer, Integer> noteMapping, int velocityMappingMin, int velocityMappingMax, boolean ignoreVelocity) {
        super(notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);
    }

    @Override
    protected void writeBytes() {
//sort the notes by key number
        notes.sort(Comparator.comparingInt(NoteEvent::getKeyNumber));

        List<List<NoteEvent>> batchedNotes = new ArrayList<>();
        for(NoteEvent note : notes) {
            boolean found = false;
            for(List<NoteEvent> batch : batchedNotes) {

                Integer currentKeyIndex = noteMapping.get(note.getKeyNumber());
                if(currentKeyIndex == null) {
                    logger.error("Failed to find key index for note " + note.getKeyNumber());
                    continue;
                }

                Integer lastBatchIndex = noteMapping.get(batch.get(batch.size() - 1).getKeyNumber());
                if(lastBatchIndex == null) {
                    logger.error("Failed to find key index for note " + batch.get(batch.size() - 1).getKeyNumber());
                    continue;
                }

                if(batch.get(0).isNoteOn() == note.isNoteOn() && batch.get(0).getVelocity() == note.getVelocity()
                        && currentKeyIndex == lastBatchIndex+1) {
                    batch.add(note);
                    found = true;
                    break;
                }
            }
            if(!found) {
                List<NoteEvent> newBatch = new ArrayList<>();
                newBatch.add(note);
                batchedNotes.add(newBatch);
            }
        }

        //B continousNum, starting, velcoity
        allocate(2 + (batchedNotes.size() * 3));
        write('B');
        write(batchedNotes.size());

        for(List<NoteEvent> batch : batchedNotes) {
            byte contiguous = (byte) batch.size(); //Key index is 0 based

            Integer keyIndex = noteMapping.get(batch.get(0).getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + batch.get(0).getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }

            byte startingKey = (byte) (int)keyIndex;
            byte velocity = (byte) batch.get(0).getVelocity();

            if(batch.get(0).isNoteOn()) {
                velocity = mapVelocity(velocity);
            }
            else {
                velocity = 0;
            }

            write(contiguous);
            write(startingKey);
            write(velocity);
        }

    }
}
