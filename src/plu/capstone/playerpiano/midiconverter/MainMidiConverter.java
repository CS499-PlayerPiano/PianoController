package plu.capstone.playerpiano.midiconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiConstants.MetaMessages;
import plu.capstone.playerpiano.sheetmusic.Note;

public class MainMidiConverter {

    static class Settings {
        public static final int MAX_NOTES_ON_AT_ONCE = 45;
        public static final long MIN_SOLENOID_TIME = 10; //in ms
        public static final long MAX_SOLENOID_TIME = 10_000; //in ms
        public static final int MIN_VELOCITY = 10;

        public static boolean ENABLED = false;

        /*
        We need some sort of algorithm that can figure out if you are holding a note, and that same not needs to be played,

        that we turn the note off ahead of time, so that the note can be played again.

        This may need to be a post processing step, but it should sound better
         */
    }



    public static void main(String[] args) throws Exception {new MainMidiConverter().run();}

    Map<Long, List<Note>> noteMap = new HashMap<>();

    Logger logger = new Logger(MainMidiConverter.class);

    File midiFile = new File("test/RUSH_E_FINAL.mid");

    int totalNotesIfWeDidntDoPostProcessing = 0;

    int notesOnRightNow = 0;

    private void run() throws Exception {


        Sequence sequence = MidiSystem.getSequence(midiFile);

        Sequence cloneSequence = new Sequence(sequence.getDivisionType(), sequence.getResolution());


        // Get the length of the song in milliseconds
        long songLengthMS = sequence.getMicrosecondLength() / 1000;

        long us_per_quarter = -1;

        for (int trackNum = 0; trackNum < sequence.getTracks().length; trackNum++) {
            Track track = sequence.getTracks()[trackNum];

            Track cloneTrack = cloneSequence.createTrack();


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

                        cloneTrack.add(event);
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

                        if(Settings.ENABLED) {
                            if (!note.isValidPianoKey()) {
                                logger.warning("Invalid piano key: " + note.getKeyNumber());
                                continue;
                            }

                            if (note.getChannelNum() == 10) {
                                logger.warning("Ignoring Channel 10 (percussion) at " + whereMS + "ms");
                                continue;
                            }

                            if (note.isNoteOn() && note.getVelocity() == 0) {
                                logger.warning("Note on with velocity 0 at " + whereMS + "ms");
                            }

                            if (notesOnRightNow >= Settings.MAX_NOTES_ON_AT_ONCE) {
                                logger.warning("Too many notes on at once at " + whereMS + "ms");
                                continue;
                            }

                            if (note.getVelocity() < Settings.MIN_VELOCITY && note.isNoteOn()) {
                                logger.warning("Velocity too low for note at " + whereMS + "ms (" + note.getVelocity() + ")");
                                continue;
                            }

                            if (note.isNoteOn()) {
                                notesOnRightNow++;
                            } else {
                                notesOnRightNow--;
                            }
                        }

                        boolean result = putNote(whereMS, note);

                        if(result) {
                            cloneTrack.add(event);
                        }

                    }


                }
            }
        }


        long amountOfNotesTotal = 0;
        long maxForTime = 0;
        for(long key : noteMap.keySet()) {
            //long tmp = noteMap.get(key).size();
            long tmp = 0;

            // We only count note ons
            for(Note note : noteMap.get(key)) {
                if(note.isNoteOn()) {
                    tmp++;
                }
            }


            amountOfNotesTotal += tmp;

            if(tmp > maxForTime) {
                maxForTime = tmp;
            }
        }

        logger.info("Finished parsing midi file");
        logger.info("Map size: " + noteMap.size());
        logger.info("Total Notes: " + amountOfNotesTotal);
        logger.info("Max Notes at one time: " + maxForTime);
        logger.info("Song Length: " + songLengthMS + "ms");


        if(Settings.ENABLED) {
            logger.debug("Total Notes if we didn't do post processing: " + totalNotesIfWeDidntDoPostProcessing);
            logger.debug("Total note loss: " + (totalNotesIfWeDidntDoPostProcessing - amountOfNotesTotal));
            MidiSystem.write(cloneSequence, 1, new File("test/RUSH_E_FINAL.CLONE.mid"));
        }

        new SheetMusicViewer(noteMap).createAndShowWindow();
    }

    boolean  putNote(long time, Note note) {
        if(!noteMap.containsKey(time)) {
            noteMap.put(time, new ArrayList<>());
        }

        if(Settings.ENABLED) {
            totalNotesIfWeDidntDoPostProcessing++;


            //only limit note ons
            if(noteMap.get(time).size() >= Settings.MAX_NOTES_ON_AT_ONCE && note.isNoteOn()) {
                logger.warning("Tried to put note on at " + time + "ms, but there are already " + noteMap.get(time).size() + " notes on at that time");
                return false;
            }


            //Can't have two on notes at the same time with different velocities
            for(Note n : noteMap.get(time)) {
                if(n.getKeyNumber() == note.getKeyNumber() && n.isNoteOn() && note.isNoteOn() && n.getVelocity() != note.getVelocity()) {
                    logger.warning("Tried to put note on at " + time + "ms, but there is already a note on at that time with a different velocity (" + n.getVelocity() + " vs " + note.getVelocity() + ")");
                    return false;
                }
            }
        }

        noteMap.get(time).add(note);
        return true;
    }

}