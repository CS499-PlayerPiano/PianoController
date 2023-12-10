package plu.capstone.playerpiano.sheetmusic;

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
            entry(0, "Bank Select"),
            entry(1, "Modulation"),
            entry(2, "Breath Controller"),
            entry(3, "Undefined"),
            entry(4, "Foot Controller"),
            entry(5, "Portamento Time"),
            entry(6, "Data Entry Most Significant Bit (MSB)"),
            entry(7, "Volume"),
            entry(8, "Balance"),
            entry(9, "Undefined"),
            entry(10, "Pan"),
            entry(11, "Expression"),
            entry(12, "Effect Controller 1"),
            entry(13, "Effect Controller 2"),
            entry(14, "Undefined"),
            entry(15, "Undefined"),
            entry(16, "General Purpose"),
            entry(20, "Undefined"),
            entry(32, "LAB (1)"),
            entry(33, "LSB (2)"),
            entry(34, "LSB (3)"),
            entry(35, "LSB (4)"),
            entry(36, "LSB (5)"),
            entry(37, "LSB (6)"),
            entry(38, "LSB (7)"),
            entry(39, "LSB (8)"),
            entry(40, "LSB (9)"),
            entry(41, "LSB (10)"),
            entry(42, "LSB (11)"),
            entry(43, "LSB (12)"),
            entry(44, "LSB (13)"),
            entry(45, "LSB (14)"),
            entry(46, "LSB (15)"),
            entry(47, "LSB (16)"),
            entry(48, "LSB (17)"),
            entry(49, "LSB (18)"),
            entry(50, "LSB (19)"),
            entry(51, "LSB (20)"),
            entry(52, "LSB (21)"),
            entry(53, "LSB (22)"),
            entry(54, "LSB (23)"),
            entry(55, "LSB (24)"),
            entry(56, "LSB (25)"),
            entry(57, "LSB (26)"),
            entry(58, "LSB (27)"),
            entry(59, "LSB (28)"),
            entry(60, "LSB (29)"),
            entry(61, "LSB (30)"),
            entry(62, "LSB (31)"),
            entry(63, "LSB (32)"),
            entry(64, "Damper Pedal / Sustain Pedal"),
            entry(65, "Portamento On/Off Switch"),
            entry(66, "Sostenuto On/Off Switch"),
            entry(67, "Soft Pedal On/Off Switch"),
            entry(68, "Legato FootSwitch"),
            entry(69, "Hold 2"),
            entry(70, "Sound Controller 1"),
            entry(71, "Sound Controller 2"),
            entry(72, "Sound Controller 3"),
            entry(73, "Sound Controller 4"),
            entry(74, "Sound Controller 5"),
            entry(75, "Sound Controller 6"),
            entry(76, "Sound Controller 7"),
            entry(77, "Sound Controller 8"),
            entry(78, "Sound Controller 9"),
            entry(79, "Sound Controller 10"),
            entry(80, "General Purpose MIDI CC Controller"),
            entry(81, "General Purpose MIDI CC Controller"),
            entry(82, "General Purpose MIDI CC Controller"),
            entry(83, "General Purpose MIDI CC Controller"),
            entry(84, "Portamento CC Control"),
            entry(88, "Undefined"),
            entry(91, "Effect 1 Depth"),
            entry(92, "Effect 2 Depth"),
            entry(93, "Effect 3 Depth"),
            entry(94, "Effect 4 Depth"),
            entry(95, "Effect 5 Depth"),
            entry(96, "(+1) Data Increment"),
            entry(97, "(-1) Data Decrement"),
            entry(98, "Non-Registered Parameter Number LSB (NRPN)"),
            entry(99, "Non-Registered Parameter Number MSB (NRPN)"),
            entry(100, "Registered Parameter Number LSB (RPN)"),
            entry(101, "Registered Parameter Number MSB (RPN)"),
            entry(102, "Undefined"),
            entry(120, "All Sound Off"),
            entry(121, "Reset All Controllers"),
            entry(122, "Local On/Off Switch"),
            entry(123, "All Notes Off"),
            entry(124, "Omni Mode Off"),
            entry(125, "Omni Mode On"),
            entry(126, "Mono Mode"),
            entry(127, "Poly Mode")
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
