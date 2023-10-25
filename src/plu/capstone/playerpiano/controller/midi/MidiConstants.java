package plu.capstone.playerpiano.controller.midi;

import java.util.Map;
import static java.util.Map.entry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A class containing constants for MIDI messages.
 */
public class MidiConstants {

    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final Map<Integer, String> CONTROL_NAMES = Map.ofEntries(
            entry(1, "Modulation Wheel"),
            entry(2, "Breath Controller"),
            entry(3, "After Touch"),
            entry(4, "Foot Controller"),
            entry(5, "Portamento Time"),
            entry(6, "Data Entry"),
            entry(7, "Main Volume"),
            entry(64, "Damper Pedal"),
            entry(65, "Portamento"),
            entry(66, "Sostenuto"),
            entry(67, "Soft Pedal"),
            entry(93, "Chorus"),
            entry(94, "Celeste"),
            entry(95, "Phaser"),
            entry(96, "Data Button Increment"),
            entry(97, "Data Button Decrement"),
            entry(122, "Local Control"),
            entry(123, "All Notes Off"),
            entry(124, "Omni Mode Off"),
            entry(125, "Omni Mode On"),
            entry(126, "Mono Mode On / Poly Mode Off"),
            entry(127, "Mono Mode Off / Poly Mode On")
    );

    public static class MetaMessages {
        public static final int SEQUENCE_NUMBER = 0x00;
        public static final int TEXT_EVENT = 0x01;
        public static final int COPYRIGHT_NOTICE = 0x02;
        public static final int SEQUENCE_NAME = 0x03;
        public static final int INSTRUMENT_NAME = 0x04;
        public static final int LYRIC = 0x05;
        public static final int MARKER = 0x06;
        public static final int CUE_POINT = 0x07;
        public static final int MIDI_CHANNEL_PREFIX = 0x20;
        public static final int END_OF_TRACK = 0x2F;
        public static final int SET_TEMPO = 0x51;
        public static final int SMPTE_OFFSET = 0x54;
        public static final int TIME_SIGNATURE = 0x58;
        public static final int KEY_SIGNATURE = 0x59;
        public static final int SEQUENCER_SPECIFIC = 0x7F;
    }

    /**
     * Converts a MIDI message to a NoteDetails object.
     * NoteDetails contains the key number, octave, and note name.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class NoteDetails {

        private final int keyNumber;
        private final int octave;
        private final String noteName;


        /**
         * Converts a MIDI key number to a NoteDetails object.
         * @param midiNote The MIDI key number.
         * @return The NoteDetails object.
         */
        public static NoteDetails from(int midiNote) {
            int octave = (midiNote / 12)-1;
            int note = midiNote % 12;
            String noteName = NOTE_NAMES[note];
            return new NoteDetails(midiNote, octave, noteName);
        }
    }

}
