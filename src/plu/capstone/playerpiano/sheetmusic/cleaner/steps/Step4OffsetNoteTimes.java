package plu.capstone.playerpiano.sheetmusic.cleaner.steps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.sheetmusic.cleaner.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

public class Step4OffsetNoteTimes implements MidiConversionStep {

    private static final boolean PRINT_DEBUG_FILES = false;

    // The custom ID for the sustain pedal. This is because the sustain pedal is not a note,
    // and we need to be able to temporarily store it in the same data structure as the notes.
    private static final int SUSTAIN_PEDAL_CUSTOM_ID = -1;

    @Override
    public String getName() {
        return "Offset note times";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        System.out.println("-------Processing sheet music-------");

        //Convert sheet music to new format
        Map<Integer, List<TimeAndNote>> newSheetMusic = fromSheetMusicToNewFormat(sheetMusic);

        for(int keyNumber : newSheetMusic.keySet()) {
            processNoteList(newSheetMusic.get(keyNumber), keyNumber);
        }

        //Just overwrite the event map, not any other variables in the sheet music.
        sheetMusic.overwriteEventMap(fromNewFormatToSheetMusic(newSheetMusic).getEventMap());
    }

    private void printTimeAndNote(List<TimeAndNote> timeAndNote, int midiKeyNumber, String fileName) {

        if(!PRINT_DEBUG_FILES){return;}

        final int indexNumber = Note.fromMidiNoteToPianoKeyIndex(midiKeyNumber);
        final boolean shouldPrint = (indexNumber == 43);

        if(!shouldPrint){return;}

        try {
            FileWriter writer = new FileWriter("tmp/" + fileName + ".txt", false);

            for(TimeAndNote tn : timeAndNote) {
                if(tn.event instanceof Note) {
                    Note n = (Note) tn.event;
                    writer.write(tn.time + " | " + n.isNoteOn() + " | " + n.getVelocity() + "\n");
                }
            }

            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void processNoteList(List<TimeAndNote> timeAndNote, final int midiKeyNumber) {


        final int shiftForwardAmount = calcShiftForwardAmount();

        //Step 0: Shift all notes forward due to beginign note being cut off
        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-0");
        for(TimeAndNote tn : timeAndNote) {
            tn.time += shiftForwardAmount;
        }

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-1");
        //Step 1: Bias ons bu the time they take to hit
        for(TimeAndNote tn : timeAndNote) {
            if(tn.event instanceof Note) {
                Note note = (Note) tn.event;
                if(note.isNoteOn()) {
                    //If the note is on, we need to offset the time it takes to hit the note
                    tn.time -= timeToHit(note);
                }
            }
        }
        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-2");

        //Step 2: Resort notes
        timeAndNote.sort(Comparator.comparingLong(a -> a.time));

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-3");
        //Step 3: Remove any two offs in a row
        for(int i = 0; i < timeAndNote.size(); i++) {
            if(i > 0) {
                if(timeAndNote.get(i).event instanceof Note && timeAndNote.get(i-1).event instanceof Note) {
                    Note note = (Note) timeAndNote.get(i).event;
                    Note previousNote = (Note) timeAndNote.get(i-1).event;
                    if(!note.isNoteOn() && !previousNote.isNoteOn()) {
                        timeAndNote.remove(i);
                        i--;
                    }
                }
            }
        }

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-4");
        //Step 4: Ensure a minimum time between off note and subsequent on note
        for(int i = 0; i < timeAndNote.size(); i++) {
            if(i > 0) {

                TimeAndNote current = timeAndNote.get(i);
                TimeAndNote previous = timeAndNote.get(i-1);

                if(current.event instanceof Note && previous.event instanceof Note) {
                    Note currentNote = (Note) current.event;
                    Note previousNote = (Note) previous.event;
                    if(currentNote.isNoteOn() && !previousNote.isNoteOn()) {
                        if(current.time - previous.time < timeToRelease(previousNote)) {
                            previous.time = current.time - timeToRelease(previousNote);

                            if(i > 1 && previous.time < timeAndNote.get(i-2).time) {
                                timeAndNote.remove(i-1);
                                i--;
                            }

                        }
                    }
                }
            }
        }

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-5");

        //Step 5: Insert Off between two consecutive ons
        for(int i = 0; i < timeAndNote.size(); i++) {
            if(i > 0) {

                TimeAndNote current = timeAndNote.get(i);
                TimeAndNote previous = timeAndNote.get(i-1);

                if(current.event instanceof Note && previous.event instanceof Note) {
                    Note currentNote = (Note) current.event;
                    Note previousNote = (Note) previous.event;
                    if(currentNote.isNoteOn() && previousNote.isNoteOn()) {

                        //If the time between the two notes is greater than the time it takes to release the previous note
                        if(current.time - previous.time > timeToRelease(previousNote)) {
                            //Insert a off note
                            final long newTime = current.time - timeToRelease(previousNote);
                            final Note newOffNote = currentNote.clone();
                            newOffNote.setNoteOn(false);
                            newOffNote.setVelocity(0);
                            timeAndNote.add(i, new TimeAndNote(newTime, newOffNote));
                            i++;
                        }
                        else {
                            //Remove the on note because there is no time to hit it
                            timeAndNote.remove(i);
                            i--;
                        }

                    }
                }
            }
        }

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-finished");

    }

    //TODO: Implement per note shift forward amount with a for loop
    private int calcShiftForwardAmount() {
        return 300;
    }

    //How long does it take to retract in MS
    //TODO: Implement this function based on note index and velocity
    private long timeToRelease(SheetMusicEvent event) {
        return 100;
    }

    //How long does it take to hit in MS
    //TODO: Implement this function based on note index and velocity
    private long timeToHit(SheetMusicEvent event) {
       return 100;
    }

    private SheetMusic fromNewFormatToSheetMusic(Map<Integer, List<TimeAndNote>> newSheetMusic) {

        List<Long> eventTimes = new ArrayList<>();

        //Get a list of all keys
        for(int keyNumber : newSheetMusic.keySet()) {
            for(TimeAndNote timeAndNote : newSheetMusic.get(keyNumber)) {
                eventTimes.add(timeAndNote.time);
            }
        }

        //Events are out of order, so we put the keys in order
        eventTimes.sort(Long::compareTo);

        SheetMusic sheetMusic = new SheetMusic();

        //Add the keys in order, with no values
        for(long time : eventTimes) {
            sheetMusic.getEventMap().put(time, new ArrayList<>());
        }

        //Add the values to the ordered keys
        for(int keyNumber : newSheetMusic.keySet()) {
            for(TimeAndNote timeAndNote : newSheetMusic.get(keyNumber)) {
                sheetMusic.putEvent(timeAndNote.time, timeAndNote.event);
            }
        }

        return sheetMusic;
    }

    private Map<Integer, List<TimeAndNote>> fromSheetMusicToNewFormat(SheetMusic sheetMusic) {

        Map<Integer, List<TimeAndNote>> newSheetMusic = new HashMap<>();

        for(long timestamp : sheetMusic.getEventMap().keySet()) {
            for(SheetMusicEvent event : sheetMusic.getEventMap().get(timestamp)) {

                int keyNumber;

                if(event instanceof Note) {
                    Note note = (Note) event;
                    keyNumber = note.getKeyNumber();
                }
                else if(event instanceof SustainPedalEvent) {
                    keyNumber = SUSTAIN_PEDAL_CUSTOM_ID;
                }
                else {
                    throw new IllegalStateException("Unknown event type: " + event.getClass().getName());
                }

                if(!newSheetMusic.containsKey(keyNumber)) {
                    newSheetMusic.put(keyNumber, new ArrayList<>());
                }

                newSheetMusic.get(keyNumber).add(new TimeAndNote(timestamp, event));

            }
        }

        //Sort the inner lists by time
        for(int keyNumber : newSheetMusic.keySet()) {
            newSheetMusic.get(keyNumber).sort(Comparator.comparingLong(a -> a.time));
        }

        return newSheetMusic;
    }

    @AllArgsConstructor
    static class TimeAndNote {
        long time;
        final SheetMusicEvent event;

    }
}
