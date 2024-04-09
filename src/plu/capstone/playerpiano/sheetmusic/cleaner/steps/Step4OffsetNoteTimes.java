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
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

public class Step4OffsetNoteTimes implements MidiConversionStep {

    private static final boolean PRINT_DEBUG_FILES = false;

    // The custom ID for the sustain pedal. This is because the sustain pedal is not a note,
    // and we need to be able to temporarily store it in the same data structure as the notes.
    private static final int SUSTAIN_PEDAL_CUSTOM_ID = -1;

    private static final Map<Integer, Integer> MIDI_ID_TO_HIT_TIME = new HashMap<>();

    static {
        //NOTE -> HIT TIME
        MIDI_ID_TO_HIT_TIME.put(21, 50);
        MIDI_ID_TO_HIT_TIME.put(22, 50);
        MIDI_ID_TO_HIT_TIME.put(23, 50);
        MIDI_ID_TO_HIT_TIME.put(24, 50);
        MIDI_ID_TO_HIT_TIME.put(25, 60);
        MIDI_ID_TO_HIT_TIME.put(26, 50);
        MIDI_ID_TO_HIT_TIME.put(27, 50);
        MIDI_ID_TO_HIT_TIME.put(28, 50);
        MIDI_ID_TO_HIT_TIME.put(29, 30);
        MIDI_ID_TO_HIT_TIME.put(30, 30);
        MIDI_ID_TO_HIT_TIME.put(31, 50);
        MIDI_ID_TO_HIT_TIME.put(32, 30);
        MIDI_ID_TO_HIT_TIME.put(33, 30);
        MIDI_ID_TO_HIT_TIME.put(33, 30);
        MIDI_ID_TO_HIT_TIME.put(34, 80);
        MIDI_ID_TO_HIT_TIME.put(35, 50);
        MIDI_ID_TO_HIT_TIME.put(36, 70);
        MIDI_ID_TO_HIT_TIME.put(37, 100); // weird
        MIDI_ID_TO_HIT_TIME.put(38, 50);
        MIDI_ID_TO_HIT_TIME.put(39, 30);
        MIDI_ID_TO_HIT_TIME.put(40, 40);
        MIDI_ID_TO_HIT_TIME.put(41, 50);
        MIDI_ID_TO_HIT_TIME.put(42, 45);
        MIDI_ID_TO_HIT_TIME.put(43, 50);
        MIDI_ID_TO_HIT_TIME.put(44, 30);
        MIDI_ID_TO_HIT_TIME.put(45, 30);
        MIDI_ID_TO_HIT_TIME.put(46, 30);
        MIDI_ID_TO_HIT_TIME.put(47, 30);

        // TODO: add notes 48 - 78




        MIDI_ID_TO_HIT_TIME.put(79, 75);
        MIDI_ID_TO_HIT_TIME.put(80, 80);
        MIDI_ID_TO_HIT_TIME.put(81, 30);
        MIDI_ID_TO_HIT_TIME.put(82, 40);
        MIDI_ID_TO_HIT_TIME.put(83, 40);
        MIDI_ID_TO_HIT_TIME.put(84, 45);
        MIDI_ID_TO_HIT_TIME.put(85, 50);
        MIDI_ID_TO_HIT_TIME.put(86, 30);
        MIDI_ID_TO_HIT_TIME.put(87, 30);
        MIDI_ID_TO_HIT_TIME.put(88, 25);
        MIDI_ID_TO_HIT_TIME.put(89, 75); // weird
        MIDI_ID_TO_HIT_TIME.put(90, 40);
        MIDI_ID_TO_HIT_TIME.put(91, 50);
        MIDI_ID_TO_HIT_TIME.put(92, 50);
        MIDI_ID_TO_HIT_TIME.put(93, 60);
        MIDI_ID_TO_HIT_TIME.put(94, 60);
        MIDI_ID_TO_HIT_TIME.put(95, 40);
        MIDI_ID_TO_HIT_TIME.put(96, 40);
        MIDI_ID_TO_HIT_TIME.put(97, 30);
        MIDI_ID_TO_HIT_TIME.put(98, 40);
        MIDI_ID_TO_HIT_TIME.put(99, 30);
        MIDI_ID_TO_HIT_TIME.put(100, 40);
        MIDI_ID_TO_HIT_TIME.put(101, 40);
        MIDI_ID_TO_HIT_TIME.put(102, 50);
        MIDI_ID_TO_HIT_TIME.put(103, 50);
        MIDI_ID_TO_HIT_TIME.put(104, 40);
        MIDI_ID_TO_HIT_TIME.put(105, 40);
        MIDI_ID_TO_HIT_TIME.put(106, 40);
        MIDI_ID_TO_HIT_TIME.put(107, 40);
        MIDI_ID_TO_HIT_TIME.put(108, 40); // does not work

    }

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

        final int indexNumber = (midiKeyNumber - 21);
        final boolean shouldPrint = (indexNumber == 43);

        if(!shouldPrint){return;}

        try {
            FileWriter writer = new FileWriter("tmp/" + fileName + ".txt", false);

            for(TimeAndNote tn : timeAndNote) {
                if(tn.event instanceof NoteEvent) {
                    NoteEvent n = (NoteEvent) tn.event;
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
            if(tn.event instanceof NoteEvent) {
                NoteEvent note = (NoteEvent) tn.event;
                if(note.isNoteOn()) {
                    //If the note is on, we need to offset the time it takes to hit the note
                    tn.time -= timeToHit(note);
                }
            }

            //We move the on and offs back my timeToHit for the sustain pedal
            else if(tn.event instanceof SustainPedalEvent) {
                tn.time -= timeToHit((SustainPedalEvent) tn.event);
            }

        }
        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-2");

        //Step 2: Resort notes
        timeAndNote.sort(Comparator.comparingLong(a -> a.time));

        printTimeAndNote(timeAndNote, midiKeyNumber, "tn-before-3");
        //Step 3: Remove any two offs in a row, or two ons in a row for the sustain pedal
        for(int i = 0; i < timeAndNote.size(); i++) {
            if(i > 0) {
                if(timeAndNote.get(i).event instanceof NoteEvent && timeAndNote.get(i-1).event instanceof NoteEvent) {
                    NoteEvent note = (NoteEvent) timeAndNote.get(i).event;
                    NoteEvent previousNote = (NoteEvent) timeAndNote.get(i-1).event;
                    if(!note.isNoteOn() && !previousNote.isNoteOn()) {
                        timeAndNote.remove(i);
                        i--;
                    }
                }


                else if(timeAndNote.get(i).event instanceof SustainPedalEvent && timeAndNote.get(i-1).event instanceof SustainPedalEvent) {
                    boolean on = ((SustainPedalEvent) timeAndNote.get(i).event).isOn();
                    boolean prevOn = ((SustainPedalEvent) timeAndNote.get(i-1).event).isOn();

                    //If they are both on or both off
                    if(on == prevOn) {
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

                //Bias previous
                if(current.event instanceof NoteEvent && previous.event instanceof NoteEvent) {
                    NoteEvent currentNote = (NoteEvent) current.event;
                    NoteEvent previousNote = (NoteEvent) previous.event;
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

                //If they are two close together
                else if(current.event instanceof SustainPedalEvent && previous.event instanceof SustainPedalEvent) {
                    SustainPedalEvent currentPedal = (SustainPedalEvent) current.event;
                    SustainPedalEvent previousPedal = (SustainPedalEvent) previous.event;
                    if(currentPedal.isOn() && !previousPedal.isOn()) {
                        if(current.time - previous.time < timeToRelease(previousPedal)) {
                            previous.time = current.time - timeToRelease(previousPedal);

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

                if(current.event instanceof NoteEvent && previous.event instanceof NoteEvent) {
                    NoteEvent currentNote = (NoteEvent) current.event;
                    NoteEvent previousNote = (NoteEvent) previous.event;
                    if(currentNote.isNoteOn() && previousNote.isNoteOn()) {

                        //If the time between the two notes is greater than the time it takes to release the previous note
                        if(current.time - previous.time > timeToRelease(previousNote)) {
                            //Insert a off note
                            final long newTime = current.time - timeToRelease(previousNote);
                            final NoteEvent newOffNote = currentNote.clone();
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

        if(event instanceof SustainPedalEvent) {
            return 450;
        }

        if(event instanceof NoteEvent) {
            NoteEvent note = (NoteEvent) event;
            return MIDI_ID_TO_HIT_TIME.getOrDefault(note.getKeyNumber(), 50);
        }

        return 100;
    }

    //How long does it take to hit in MS
    //TODO: Implement this function based on note index and velocity
    private long timeToHit(SheetMusicEvent event) {

        if(event instanceof SustainPedalEvent) {
            return 450;
        }

        return 0;
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

                if(event instanceof NoteEvent) {
                    NoteEvent note = (NoteEvent) event;
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
