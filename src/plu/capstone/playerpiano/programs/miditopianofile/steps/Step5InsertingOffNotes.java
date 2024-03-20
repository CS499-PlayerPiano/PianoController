package plu.capstone.playerpiano.programs.miditopianofile.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.programs.miditopianofile.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

@AllArgsConstructor
public class Step5InsertingOffNotes implements MidiConversionStep {

    // Amount of time before a duplicated on note, to insert an off note
    private final long DUPLICATE_ON_LAG_TIME;

    @Override
    public String getName() {
        return "Inserting off notes";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        long[] lastTimeNoteOn = new long[88];
        for(int i = 0; i < 88; i++) {
            lastTimeNoteOn[i] = -1;
        }

        Map<Long, List<Note>> notesToAdd = new HashMap<>();
        Map<Long, List<Note>> notesToRemove = new HashMap<>();


        for(long timestamp : sheetMusic.getEventMap().keySet()) {
            for(SheetMusicEvent event : sheetMusic.getEventMap().get(timestamp)) {

                if (event instanceof Note) {
                    Note note = (Note) event;

                    if (note.isNoteOn()) {

                        boolean didWeRemoveThisNote = false;

                        //last time the note was on
                        if(lastTimeNoteOn[note.toPianoKey()] != -1) {
                            final long timeSinceLastNoteOn = timestamp - lastTimeNoteOn[note.toPianoKey()];
                            if(timeSinceLastNoteOn > DUPLICATE_ON_LAG_TIME) {
                                //TODO: Insert off note @ (timestamp - DUPLICATE_ON_LAG_TIME)

                                final long newTimestamp = timestamp - DUPLICATE_ON_LAG_TIME;

                                if(!notesToAdd.containsKey(newTimestamp)) {
                                    notesToAdd.put(newTimestamp, new ArrayList<>());
                                }

                                Note offNote = note.clone();
                                offNote.setNoteOn(false);

                                notesToAdd.get(newTimestamp).add(offNote);

                            }
                            else {
                                //TODO: Remove the current on note.

                                if(!notesToRemove.containsKey(timestamp)) {
                                    notesToRemove.put(timestamp, new ArrayList<>());
                                }

                                notesToRemove.get(timestamp).add(note);

                            }
                        }

                        if(!didWeRemoveThisNote) {
                            lastTimeNoteOn[note.toPianoKey()] = timestamp;
                        }
                    }
                    else {
                        lastTimeNoteOn[note.toPianoKey()] = -1;
                    }
                }
            }
        }

        // Add all the notes we need to add
        for(long timestamp : notesToAdd.keySet()) {
            System.out.println("Adding " + notesToAdd.get(timestamp).size() + " off notes at " + timestamp);
            for(Note note : notesToAdd.get(timestamp)) {
                sheetMusic.putEvent(timestamp, note);
            }
        }

        // Remove all the notes we need to remove
        for(long timestamp : notesToRemove.keySet()) {
            System.out.println("Removing " + notesToRemove.get(timestamp).size() + " notes at " + timestamp);
            sheetMusic.getEventMap().get(timestamp).removeAll(notesToRemove.get(timestamp));
        }

    }
}
