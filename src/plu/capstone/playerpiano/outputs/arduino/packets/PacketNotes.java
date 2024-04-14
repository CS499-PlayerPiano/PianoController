package plu.capstone.playerpiano.outputs.arduino.packets;

import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.utilities.MathUtilities;

//Abstract class for packets that contain note events
public abstract class PacketNotes extends Packet {

    protected final List<NoteEvent> notes;
    protected final Map<Integer, Integer> noteMapping;

    private final int velocityMappingMin;
    private final int velocityMappingMax;
    private final boolean ignoreVelocity;

    public PacketNotes(List<NoteEvent> notes, Map<Integer, Integer> noteMapping, int velocityMappingMin, int velocityMappingMax, boolean ignoreVelocity) {
        this.notes = notes;
        this.noteMapping = noteMapping;
        this.velocityMappingMin = velocityMappingMin;
        this.velocityMappingMax = velocityMappingMax;
        this.ignoreVelocity = ignoreVelocity;
    }

    protected final byte mapVelocity(int velocity) {
        if(ignoreVelocity) {
            return (byte) velocityMappingMax;
        }
        return (byte) MathUtilities.map(velocity, 0, 127, velocityMappingMin, velocityMappingMax);
    }

}
