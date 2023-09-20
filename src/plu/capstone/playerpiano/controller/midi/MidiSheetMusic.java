package plu.capstone.playerpiano.controller.midi;

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
import plu.capstone.playerpiano.controller.logger.Logger;
import plu.capstone.playerpiano.controller.midi.MidiConstants.MetaMessages;

public class MidiSheetMusic extends SheetMusic {

    private final Logger logger = new Logger(this);

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

        long us_per_quarter = -1;

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
                        logger.debug("Tempo change: " + tempo);
                    }
                    //MidiMessagePrinter.printMetaMessage(-1, mm);
                }

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;

                    if(sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) {

                        Note note = Note.fromMidiMessage(sm);

                        // Calculate the time in milliseconds of the note
                        long tick = event.getTick();
                        long ticks_per_quarter = sequence.getResolution();
                        long us_per_tick = us_per_quarter / ticks_per_quarter;
                        long where = tick * us_per_tick;
                        long whereMS = where / 1000;

                        if(note.isNoteOn() && note.getVelocity() == 0) {
                            logger.warning("Note on with velocity 0 at " + whereMS + "ms");
                        }
                        putNote(whereMS, note);
                    }


                }
            }
        }

        logger.debug("Finished parsing midi file");
        long amountOfNotesTotal = 0;
        long maxForTime = 0;
        for(long key : getNoteMap().keySet()) {
            long tmp = getNoteMap().get(key).size();
            amountOfNotesTotal += tmp;

            if(tmp > maxForTime) {
                maxForTime = tmp;
            }
        }
        logger.debug("Total Notes: " + amountOfNotesTotal);
        logger.debug("Max Notes at one time: " + maxForTime);
    }

}
