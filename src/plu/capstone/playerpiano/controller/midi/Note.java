package plu.capstone.playerpiano.controller.midi;

import javax.sound.midi.ShortMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a single note in a piece of sheet music.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Note {

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

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
    private final int velocity;

    /**
     * Whether this note is on or off.
     */
    private final boolean noteOn;

    /**
     * Creates a new Note object.
     * Use {@link #fromMidiMessage(ShortMessage)} to create a Note from a ShortMessage.
     * @param keyNumber The MIDI key number of this note.
     * @param velocity The Midi velocity of this note.
     * @param noteOn Whether this note is on or off.
     */
    public Note(byte keyNumber, byte velocity, boolean noteOn) {
        this(keyNumber, velocity, noteOn, NO_CHANNEL);
    }
    public Note(byte keyNumber, byte velocity, boolean noteOn, int channelNum) {
        this.keyNumber = keyNumber;
        this.octave = (keyNumber / 12)-1;
        int note = keyNumber % 12;
        this.noteName = NOTE_NAMES[note];
        this.velocity = velocity;
        this.noteOn = noteOn;
        this.channelNum = channelNum;
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
        return keyNumber >= 21 && keyNumber <= 88;
    }

    /**
     * Converts the midi key number to a piano index.
     * @return the piano index number of this note.
     */
    public int toPianoKey() {
        return keyNumber - 21;
    }

}
