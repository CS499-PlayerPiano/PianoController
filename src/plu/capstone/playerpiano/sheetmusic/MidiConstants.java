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

    public static class ControlMessages {
        public static final int BANK_SELECT = 0;
        public static final int MODULATION = 1;
        public static final int BREATH_CONTROLLER = 2;
        public static final int FOOT_CONTROLLER = 4;
        public static final int PORTAMENTO_TIME = 5;
        public static final int DATA_ENTRY_MSB = 6;
        public static final int VOLUME = 7;
        public static final int BALANCE = 8;
        public static final int PAN = 10;
        public static final int EXPRESSION = 11;
        public static final int EFFECT_CONTROLLER_1 = 12;
        public static final int EFFECT_CONTROLLER_2 = 13;
        public static final int GENERAL_PURPOSE = 16;
        public static final int LAB_1 = 32;
        public static final int LSB_2 = 33;
        public static final int LSB_3 = 34;
        public static final int LSB_4 = 35;
        public static final int LSB_5 = 36;
        public static final int LSB_6 = 37;
        public static final int LSB_7 = 38;
        public static final int LSB_8 = 39;
        public static final int LSB_9 = 40;
        public static final int LSB_10 = 41;
        public static final int LSB_11 = 42;
        public static final int LSB_12 = 43;
        public static final int LSB_13 = 44;
        public static final int LSB_14 = 45;
        public static final int LSB_15 = 46;
        public static final int LSB_16 = 47;
        public static final int LSB_17 = 48;
        public static final int LSB_18 = 49;
        public static final int LSB_19 = 50;
        public static final int LSB_20 = 51;
        public static final int LSB_21 = 52;
        public static final int LSB_22 = 53;
        public static final int LSB_23 = 54;
        public static final int LSB_24 = 55;
        public static final int LSB_25 = 56;
        public static final int LSB_26 = 57;
        public static final int LSB_27 = 58;
        public static final int LSB_28 = 59;
        public static final int LSB_29 = 60;
        public static final int LSB_30 = 61;
        public static final int LSB_31 = 62;
        public static final int LSB_32 = 63;
        public static final int DAMPER_PEDAL = 64;
        public static final int PORTAMENTO_SWITCH = 65;
        public static final int SOSTENUTO_SWITCH = 66;
        public static final int SOFT_PEDAL_SWITCH = 67;
        public static final int LEGATO_FOOTSWITCH = 68;
        public static final int HOLD_2 = 69;
        public static final int SOUND_CONTROLLER_1 = 70;
        public static final int SOUND_CONTROLLER_2 = 71;
        public static final int SOUND_CONTROLLER_3 = 72;
        public static final int SOUND_CONTROLLER_4 = 73;
        public static final int SOUND_CONTROLLER_5 = 74;
        public static final int SOUND_CONTROLLER_6 = 75;
        public static final int SOUND_CONTROLLER_7 = 76;
        public static final int SOUND_CONTROLLER_8 = 77;
        public static final int SOUND_CONTROLLER_9 = 78;
        public static final int SOUND_CONTROLLER_10 = 79;
        public static final int GENERAL_PURPOSE_80 = 80;
        public static final int GENERAL_PURPOSE_81 = 81;
        public static final int GENERAL_PURPOSE_82 = 82;
        public static final int GENERAL_PURPOSE_83 = 83;
        public static final int PORTAMENTO_CC_CONTROL = 84;
        public static final int EFFECT_1_DEPTH = 91;
        public static final int EFFECT_2_DEPTH = 92;
        public static final int EFFECT_3_DEPTH = 93;
        public static final int EFFECT_4_DEPTH = 94;
        public static final int EFFECT_5_DEPTH = 95;
        public static final int DATA_INCREMENT = 96;
        public static final int DATA_DECREMENT = 97;
        public static final int NRPN_LSB = 98;
        public static final int NRPN_MSB = 99;
        public static final int RPN_LSB = 100;
        public static final int RPN_MSB = 101;
        public static final int UNDEFINED = 102;
        public static final int ALL_SOUND_OFF = 120;
        public static final int RESET_ALL_CONTROLLERS = 121;
        public static final int LOCAL_ON_OFF_SWITCH = 122;
        public static final int ALL_NOTES_OFF = 123;
        public static final int OMNI_MODE_OFF = 124;
        public static final int OMNI_MODE_ON = 125;
        public static final int MONO_MODE = 126;
        public static final int POLY_MODE = 127;

        private static final Map<Integer, String> CONTROL_NAMES = Map.ofEntries(
                entry(BANK_SELECT, "Bank Select"),
                entry(MODULATION, "Modulation"),
                entry(BREATH_CONTROLLER, "Breath Controller"),
                entry(FOOT_CONTROLLER, "Foot Controller"),
                entry(PORTAMENTO_TIME, "Portamento Time"),
                entry(DATA_ENTRY_MSB, "Data Entry Most Significant Bit (MSB)"),
                entry(VOLUME, "Volume"),
                entry(BALANCE, "Balance"),
                entry(PAN, "Pan"),
                entry(EXPRESSION, "Expression"),
                entry(EFFECT_CONTROLLER_1, "Effect Controller 1"),
                entry(EFFECT_CONTROLLER_2, "Effect Controller 2"),
                entry(GENERAL_PURPOSE, "General Purpose"),
                entry(LAB_1, "LAB (1)"),
                entry(LSB_2, "LSB (2)"),
                entry(LSB_3, "LSB (3)"),
                entry(LSB_4, "LSB (4)"),
                entry(LSB_5, "LSB (5)"),
                entry(LSB_6, "LSB (6)"),
                entry(LSB_7, "LSB (7)"),
                entry(LSB_8, "LSB (8)"),
                entry(LSB_9, "LSB (9)"),
                entry(LSB_10, "LSB (10)"),
                entry(LSB_11, "LSB (11)"),
                entry(LSB_12, "LSB (12)"),
                entry(LSB_13, "LSB (13)"),
                entry(LSB_14, "LSB (14)"),
                entry(LSB_15, "LSB (15)"),
                entry(LSB_16, "LSB (16)"),
                entry(LSB_17, "LSB (17)"),
                entry(LSB_18, "LSB (18)"),
                entry(LSB_19, "LSB (19)"),
                entry(LSB_20, "LSB (20)"),
                entry(LSB_21, "LSB (21)"),
                entry(LSB_22, "LSB (22)"),
                entry(LSB_23, "LSB (23)"),
                entry(LSB_24, "LSB (24)"),
                entry(LSB_25, "LSB (25)"),
                entry(LSB_26, "LSB (26)"),
                entry(LSB_27, "LSB (27)"),
                entry(LSB_28, "LSB (28)"),
                entry(LSB_29, "LSB (29)"),
                entry(LSB_30, "LSB (30)"),
                entry(LSB_31, "LSB (31)"),
                entry(LSB_32, "LSB (32)"),
                entry(DAMPER_PEDAL, "Damper Pedal / Sustain Pedal"),
                entry(PORTAMENTO_SWITCH, "Portamento On/Off Switch"),
                entry(SOSTENUTO_SWITCH, "Sostenuto On/Off Switch"),
                entry(SOFT_PEDAL_SWITCH, "Soft Pedal On/Off Switch"),
                entry(LEGATO_FOOTSWITCH, "Legato FootSwitch"),
                entry(HOLD_2, "Hold 2"),
                entry(SOUND_CONTROLLER_1, "Sound Controller 1"),
                entry(SOUND_CONTROLLER_2, "Sound Controller 2"),
                entry(SOUND_CONTROLLER_3, "Sound Controller 3"),
                entry(SOUND_CONTROLLER_4, "Sound Controller 4"),
                entry(SOUND_CONTROLLER_5, "Sound Controller 5"),
                entry(SOUND_CONTROLLER_6, "Sound Controller 6"),
                entry(SOUND_CONTROLLER_7, "Sound Controller 7"),
                entry(SOUND_CONTROLLER_8, "Sound Controller 8"),
                entry(SOUND_CONTROLLER_9, "Sound Controller 9"),
                entry(SOUND_CONTROLLER_10, "Sound Controller 10"),
                entry(GENERAL_PURPOSE_80, "General Purpose MIDI CC Controller"),
                entry(GENERAL_PURPOSE_81, "General Purpose MIDI CC Controller"),
                entry(GENERAL_PURPOSE_82, "General Purpose MIDI CC Controller"),
                entry(GENERAL_PURPOSE_83, "General Purpose MIDI CC Controller"),
                entry(PORTAMENTO_CC_CONTROL, "Portamento CC Control"),
                entry(EFFECT_1_DEPTH, "Effect 1 Depth"),
                entry(EFFECT_2_DEPTH, "Effect 2 Depth"),
                entry(EFFECT_3_DEPTH, "Effect 3 Depth"),
                entry(EFFECT_4_DEPTH, "Effect 4 Depth"),
                entry(EFFECT_5_DEPTH, "Effect 5 Depth"),
                entry(DATA_INCREMENT, "(+1) Data Increment"),
                entry(DATA_DECREMENT, "(-1) Data Decrement"),
                entry(NRPN_LSB, "Non-Registered Parameter Number LSB (NRPN)"),
                entry(NRPN_MSB, "Non-Registered Parameter Number MSB (NRPN)"),
                entry(RPN_LSB, "Registered Parameter Number LSB (RPN)"),
                entry(RPN_MSB, "Registered Parameter Number MSB (RPN)"),
                entry(UNDEFINED, "Undefined"),
                entry(ALL_SOUND_OFF, "All Sound Off"),
                entry(RESET_ALL_CONTROLLERS, "Reset All Controllers"),
                entry(LOCAL_ON_OFF_SWITCH, "Local On/Off Switch"),
                entry(ALL_NOTES_OFF, "All Notes Off"),
                entry(OMNI_MODE_OFF, "Omni Mode Off"),
                entry(OMNI_MODE_ON, "Omni Mode On"),
                entry(MONO_MODE, "Mono Mode"),
                entry(POLY_MODE, "Poly Mode")

        );

        public static String getControlName(int control) {
            return CONTROL_NAMES.getOrDefault(control, "Undefined (" + control + ")");
        }
    }

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
