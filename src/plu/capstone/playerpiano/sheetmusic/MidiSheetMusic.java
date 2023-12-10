package plu.capstone.playerpiano.sheetmusic;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiConstants.ControlMessages;
import plu.capstone.playerpiano.sheetmusic.MidiConstants.MetaMessages;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEffect;
import plu.capstone.playerpiano.sheetmusic.events.TempoChangeEvent;

/**
 * This class is used to parse midi files into SheetMusic
 */
public class MidiSheetMusic extends SheetMusic {

    private final Logger logger = new Logger(this);

    /**
     * Creates a new MidiSheetMusic object from a midi file
     * @param midiFile The midi file to parse
     * @throws javax.sound.midi.InvalidMidiDataException If the midi file is invalid
     * @throws java.io.IOException If the midi file cannot be read
     */
    public MidiSheetMusic(File midiFile) throws InvalidMidiDataException, IOException {
        load(midiFile);
    }

    private void load(File midiFile) throws InvalidMidiDataException, IOException {

        if(!midiFile.exists()) {
            throw new IOException("File does not exist");
        }

        Sequence sequence = MidiSystem.getSequence(midiFile);

        // Get the length of the song in milliseconds
        songLengthMS = sequence.getMicrosecondLength() / 1000;

        //Default tempo is 500,000 microseconds per quarter note, or 120 BPM
        long us_per_quarter = 500_000;

        for (int trackNum = 0; trackNum < sequence.getTracks().length; trackNum++) {
            Track track = sequence.getTracks()[trackNum];
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    if (mm.getType() == MetaMessages.SET_TEMPO) {

                        //Get the tempo from the meta message, so we can calculate the time of each note
                        byte[] data = mm.getData();
                        int tempo = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                        us_per_quarter = tempo;

                        // Calculate the time in milliseconds of the note
                        long tick = event.getTick();
                        long ticks_per_quarter = sequence.getResolution();
                        long us_per_tick = us_per_quarter / ticks_per_quarter;
                        long where = tick * us_per_tick;
                        long whereMS = where / 1000;

                        logger.debug("Tempo change: " + tempo);
                        putEvent(whereMS, new TempoChangeEvent(tempo));
                    }
                }

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;

                    // Calculate the time in milliseconds of the event
                    long tick = event.getTick();
                    long ticks_per_quarter = sequence.getResolution();
                    long us_per_tick = us_per_quarter / ticks_per_quarter;
                    long where = tick * us_per_tick;
                    long whereMS = where / 1000;

                    if(sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) {

                        Note note = Note.fromMidiMessage(sm);

                        if(note.isNoteOn() && note.getVelocity() == 0) {
                            logger.warning("Note on with velocity 0 at " + whereMS + "ms");
                        }
                        putEvent(whereMS, note);
                    }

                    else if(sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                        if(sm.getData1() == ControlMessages.DAMPER_PEDAL) {
                            boolean on = sm.getData2() >= 64; //0-63 is off, 64-127 is on
                            logger.debug("Sustain pedal: " + on);
                            putEvent(event.getTick(), new SustainPedalEffect(on));
                        }
                    }

                    //Test to see if the midi file has any control changes for the pedals.
                    //Most midi songs seem to fake the pedal by just holding the notes for a long time.
                    else if(sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                        int controller = sm.getData1();
                        int value = sm.getData2();
                        String name = ControlMessages.getControlName(controller);
                        logger.debug("Control change: " + ConsoleColors.PURPLE_BRIGHT + name + ConsoleColors.RESET + " value: " + ConsoleColors.PURPLE_BRIGHT + value + ConsoleColors.RESET);
                    }

                }
            }
        }

        logger.debug("Finished parsing midi file");
        long amountOfNotesTotal = 0;
        long maxForTime = 0;
        for(long key : getEventMap().keySet()) {
            long tmp = getEventMap().get(key).size();
            amountOfNotesTotal += tmp;

            if(tmp > maxForTime) {
                maxForTime = tmp;
            }
        }
        logger.debug("Total Notes: " + amountOfNotesTotal);
        logger.debug("Max Notes at one time: " + maxForTime);
    }

}
