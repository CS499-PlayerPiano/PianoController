package plu.capstone.playerpiano.controller.midi;

import javax.sound.midi.ShortMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Note {

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static final int NO_CHANNEL = -1;

    private int channelNum = NO_CHANNEL;

    private final int keyNumber;
    private final int octave;
    private final String noteName;
    private final int velocity;
    private final boolean noteOn;

    public Note(byte keyNumber, byte velocity, boolean noteOn) {
        this.keyNumber = keyNumber;
        this.octave = (keyNumber / 12)-1;
        int note = keyNumber % 12;
        this.noteName = NOTE_NAMES[note];
        this.velocity = velocity;
        this.noteOn = noteOn;
    }

    public static Note fromMidiMessage(ShortMessage sm) {
        if(sm.getCommand() != ShortMessage.NOTE_ON && sm.getCommand() != ShortMessage.NOTE_OFF) {
            throw new IllegalArgumentException("ShortMessage must be a NOTE_ON or NOTE_OFF command");
        }

        byte vel = (byte) sm.getData2();
        Note note = new Note(
                (byte) sm.getData1(),
                vel,
                sm.getCommand() == ShortMessage.NOTE_ON && vel > 0);

        note.channelNum = sm.getChannel();

        return note;
    }

    public boolean isValidPianoKey() {
        return keyNumber >= 21 && keyNumber <= 88;
    }

    public int toPianoKey() {
        return keyNumber - 21;
    }

}
