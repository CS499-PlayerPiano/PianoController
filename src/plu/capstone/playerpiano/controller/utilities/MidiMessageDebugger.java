package plu.capstone.playerpiano.controller.utilities;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiConstants;
import plu.capstone.playerpiano.sheetmusic.MidiConstants.MetaMessages;

public class MidiMessageDebugger {

    private MidiMessageDebugger() {}

    private static final Logger LOGGER = new Logger(MidiMessageDebugger.class);

    public static void printMessage(MidiMessage message) {
        if(message instanceof ShortMessage) {
            printShortMessage((ShortMessage) message);
        }
        else if(message instanceof SysexMessage) {
            printSysexMessage((SysexMessage) message);
        }
        else if(message instanceof MetaMessage) {
            printMetaMessage((MetaMessage) message);
        }
        else {
            LOGGER.info("Unknown message type: " + message.getClass().getName());
        }
    }

    public static void printMetaMessage(MetaMessage mm) {

        final String PREFIX = ConsoleColors.CYAN + "[MM] " + ConsoleColors.WHITE;

        switch (mm.getType()) {
            case MetaMessages.SEQUENCE_NUMBER:
                LOGGER.info(PREFIX + "Sequence number: " + ConsoleColors.RESET + mm.getData()[0] + " " + mm.getData()[1]);
                break;
            case MetaMessages.TEXT_EVENT:
                LOGGER.info(PREFIX + "Text event: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.COPYRIGHT_NOTICE:
                LOGGER.info(PREFIX + "Copy right notice: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.SEQUENCE_NAME:
                LOGGER.info(PREFIX + "Sequence name: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.INSTRUMENT_NAME:
                LOGGER.info(PREFIX + "Instrument name: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.LYRIC:
                LOGGER.info(PREFIX + "Lyric: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.MARKER:
                LOGGER.info(PREFIX + "Marker: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.CUE_POINT:
                LOGGER.info(PREFIX + "Cue point: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            case MetaMessages.MIDI_CHANNEL_PREFIX:
                LOGGER.info(PREFIX + "MIDI channel prefix: " + ConsoleColors.RESET + mm.getData()[0]);
                break;
            case MetaMessages.END_OF_TRACK:
                LOGGER.info(PREFIX + "End of track");
                break;
            case MetaMessages.SET_TEMPO:

                int value = 0;
                for (byte b : mm.getData()) {
                    value = (value << 8) + (b & 0xFF);
                }
                LOGGER.info(PREFIX + "Set tempo: " + ConsoleColors.RESET + value);
                break;
            case MetaMessages.SMPTE_OFFSET:
                LOGGER.info(PREFIX + "SMPTE offset: " + ConsoleColors.RESET + mm.getData()[0] + " " + mm.getData()[1] + " " + mm.getData()[2] + " " + mm.getData()[3] + " " + mm.getData()[4]);
                break;
            case MetaMessages.TIME_SIGNATURE:
                LOGGER.info(PREFIX + "Time signature: " + ConsoleColors.RESET + mm.getData()[0] + " " + mm.getData()[1] + " " + mm.getData()[2] + " " + mm.getData()[3]);
                break;
            case MetaMessages.KEY_SIGNATURE:
                LOGGER.info(PREFIX + "Key signature: " + ConsoleColors.RESET + mm.getData()[0] + " " + mm.getData()[1]);
                break;
            case MetaMessages.SEQUENCER_SPECIFIC:
                LOGGER.info(PREFIX + "Sequencer specific: " + ConsoleColors.RESET + new String(mm.getData()));
                break;
            default:
                LOGGER.info(PREFIX + "Unknown: " + ConsoleColors.RESET + mm.getType());
        }
    }

    public static void printSysexMessage(SysexMessage sm) {
        LOGGER.info("[SX] " + ConsoleColors.RESET + bytesToHex(sm.getData()));
    }

    public static void printShortMessage(ShortMessage sm) {
        final String PREFIX = ConsoleColors.PURPLE_BRIGHT + "[SM] " + ConsoleColors.RESET + "Channel: " + ConsoleColors.PURPLE_BRIGHT + sm.getChannel() + ConsoleColors.RESET + " ";
        if (sm.getCommand() == ShortMessage.NOTE_ON) {
            int key = sm.getData1();
            int octave = (key / 12)-1;
            int note = key % 12;
            String noteName = MidiConstants.NOTE_NAMES[note];
            int velocity = sm.getData2();


            if(velocity == 0) {
                LOGGER.info(PREFIX + "Note " + ConsoleColors.RED_BRIGHT + "OFF" + ConsoleColors.RESET + ", " + ConsoleColors.PURPLE_BRIGHT + noteName + octave + ConsoleColors.RESET + " key: " + ConsoleColors.PURPLE_BRIGHT + key + ConsoleColors.RESET + " velocity: " + ConsoleColors.PURPLE_BRIGHT + velocity + ConsoleColors.RESET);
            }
            else {
                LOGGER.info(PREFIX + "Note " + ConsoleColors.GREEN_BRIGHT +"ON" + ConsoleColors.RESET + ", " + ConsoleColors.PURPLE_BRIGHT + noteName + octave + ConsoleColors.RESET + " key: " + ConsoleColors.PURPLE_BRIGHT + key + ConsoleColors.RESET + " velocity: " + ConsoleColors.PURPLE_BRIGHT + velocity + ConsoleColors.RESET);
            }
        }
        else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
            int key = sm.getData1();
            int octave = (key / 12)-1;
            int note = key % 12;
            String noteName = MidiConstants.NOTE_NAMES[note];
            int velocity = sm.getData2();
            LOGGER.info(PREFIX + "Note " + ConsoleColors.RED_BRIGHT + "OFF" + ConsoleColors.RESET + ", " + ConsoleColors.PURPLE_BRIGHT + noteName + octave + ConsoleColors.RESET + " Key: " + ConsoleColors.PURPLE_BRIGHT + key + ConsoleColors.RESET + " velocity: " + ConsoleColors.PURPLE_BRIGHT + velocity + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
            int controller = sm.getData1();
            int value = sm.getData2();
            String name = MidiConstants.ControlMessages.getControlName(controller);
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Control change: " + ConsoleColors.PURPLE_BRIGHT + name + ConsoleColors.RESET + " Value: " + ConsoleColors.PURPLE_BRIGHT + value + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.CHANNEL_PRESSURE) {
            int value = sm.getData1();
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Channel pressure: " + ConsoleColors.PURPLE_BRIGHT + value + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.PITCH_BEND) {
            int value = sm.getData1() + (sm.getData2() << 7);
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Pitch bend: " + ConsoleColors.PURPLE_BRIGHT + value + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.POLY_PRESSURE) {
            int key = sm.getData1();
            int octave = (key / 12)-1;
            int note = key % 12;
            String noteName = MidiConstants.NOTE_NAMES[note];
            int value = sm.getData2();
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Poly pressure: " + ConsoleColors.PURPLE_BRIGHT + noteName + octave + ConsoleColors.RESET + " key: " + ConsoleColors.PURPLE_BRIGHT + key + ConsoleColors.RESET + " value: " + ConsoleColors.PURPLE_BRIGHT + value + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.STOP) {
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Stop" + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.START) {
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Start" + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
            int program = sm.getData1();
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Program change: " + ConsoleColors.PURPLE_BRIGHT + program + ConsoleColors.RESET);
        }
        else if(sm.getCommand() == ShortMessage.TIMING_CLOCK) {
            LOGGER.info(PREFIX + ConsoleColors.YELLOW_BRIGHT + "Timing Clock" + ConsoleColors.RESET);
        }
        else {
            LOGGER.info(PREFIX + "Other Command: " + ConsoleColors.PURPLE_BRIGHT + sm.getCommand());
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
