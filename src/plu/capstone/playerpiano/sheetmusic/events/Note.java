package plu.capstone.playerpiano.sheetmusic.events;

import javax.sound.midi.ShortMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import plu.capstone.playerpiano.sheetmusic.MidiConstants.NoteDetails;

/**
 * Represents a single note in a piece of sheet music.
 */
@Getter
@ToString
public class Note implements SheetMusicEvent, Cloneable {

    public static final byte MAX_VELOCITY = 127;
    public static final byte MIN_VELOCITY = 0;

    public static final int NO_CHANNEL = -1;

    /**
     * The channel number of this note.
     * If no channel is specified, this will be {@link #NO_CHANNEL}.
     */
    private final int channelNum;

    /**
     * The MIDI key number of this note.
     * TODO: This should be a byte...
     */
    private final int keyNumber;

    /**
     * The octave of this note.
     */
    private final int octave;

    /**
     * The name of this note.
     */
    private final String noteName;

    /**
     * The velocity of this note.
     * Byte from 0-255
     * TODO: This should be a byte...
     */
    @Setter private int velocity;

    /**
     * Whether this note is on or off.
     */
    @Setter
    private boolean noteOn;


    @Getter
    private boolean isBlackKey;

    /**
     * Creates a new Note object.
     * Use {@link #fromMidiMessage(javax.sound.midi.ShortMessage)} to create a Note from a ShortMessage.
     * @param keyNumber The MIDI key number of this note.
     * @param velocity The Midi velocity of this note.
     * @param noteOn Whether this note is on or off.
     */
    public Note(byte keyNumber, byte velocity, boolean noteOn) {
        this(keyNumber, velocity, noteOn, NO_CHANNEL);
    }
    public Note(byte keyNumber, byte velocity, boolean noteOn, int channelNum) {
        this.keyNumber = keyNumber;

        final NoteDetails noteDetails = NoteDetails.from(keyNumber);
        this.octave = noteDetails.getOctave();
        this.noteName = noteDetails.getNoteName();
        this.velocity = velocity;
        this.noteOn = noteOn;
        this.channelNum = channelNum;

        if(this.velocity > 127) {
            this.velocity = 127;
        }

        this.isBlackKey = isBlackKey(keyNumber);
    }

    private static boolean isBlackKey(int keyNumber) {
        int key = keyNumber % 12;
        return key == 1 || key == 3 || key == 6 || key == 8 || key == 10;
    }

    /**
     * Creates a new Note object from a ShortMessage.
     * @param sm The ShortMessage to create a Note from.
     * @return The Note created from the ShortMessage.
     */
    public static Note fromMidiMessage(ShortMessage sm) {
        if(sm.getCommand() != ShortMessage.NOTE_ON && sm.getCommand() != ShortMessage.NOTE_OFF) {
            throw new IllegalArgumentException("ShortMessage must be a NOTE_ON or NOTE_OFF command");
        }

        byte vel = (byte) sm.getData2();
        boolean noteOn = sm.getCommand() == ShortMessage.NOTE_ON && vel > 0;
        Note note = new Note(
                (byte) sm.getData1(),
                vel,
                noteOn,
                sm.getChannel());

        return note;
    }

    /**
     * Checks if this note is a valid piano key.
     * @return true if this note is a valid piano key, false otherwise.
     */
    public boolean isValidPianoKey() {
        return keyNumber >= 21 && keyNumber <= 108;
    }

    /**
     * Converts the midi key number to a piano index.
     * @return the piano index number of this note. 0-87
     */
    public int toPianoKey() {
        return Note.fromMidiNoteToPianoKeyIndex(keyNumber);
    }

    /**
     * Converts the piano index to a midi key number.
     * @param midiKeyNumber The midi key number to convert.
     * @return the midi key number of this note. 0 - 87
     */
    public static int fromMidiNoteToPianoKeyIndex(int midiKeyNumber) {
        return midiKeyNumber - 21;
    }

    /**
     * Converts the piano index to a midi key number.
     * @param keyNumber The piano key index to convert 0-87
     * @return the midi key number of this note.
     */
    public static byte fromPianoKeyIndexToMidiNote(int keyNumber) {
        return (byte) (keyNumber + 21);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (channelNum != note.channelNum) return false;
        if (velocity != note.velocity) return false;
        return noteOn == note.noteOn;
    }

    @Override
    public int hashCode() {
        int result = channelNum;
        result = 31 * result + velocity;
        result = 31 * result + (noteOn ? 1 : 0);
        return result;
    }

    @Override
    public byte getEventTypeId() {
        return EVENT_NOTE;
    }

    @Override
    public Note clone() {
        return new Note(
                (byte) keyNumber,
                (byte) velocity,
                noteOn,
                channelNum);
    }
}
