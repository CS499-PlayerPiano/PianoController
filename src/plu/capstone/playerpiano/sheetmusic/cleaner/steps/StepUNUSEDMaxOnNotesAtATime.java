package plu.capstone.playerpiano.sheetmusic.cleaner.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.sheetmusic.cleaner.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

@AllArgsConstructor
public class StepUNUSEDMaxOnNotesAtATime implements MidiConversionStep {

    private final int MAX_NOTES_ON;

    @Override
    public String getName() {
        return "Max on notes at a time";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        int notesOn = 0;
        Map<Long, List<NoteEvent>> noteToBeRemoved = new HashMap<>();
        boolean[] isNoteOn = new boolean[88];

        for(int i = 0; i < 88; i++) {
            isNoteOn[i] = false;
        }

        for (long timestamp : sheetMusic.getEventMap().keySet()) {
            for (SheetMusicEvent event : sheetMusic.getEventMap().get(timestamp)) {

                if(event instanceof NoteEvent) {

                    NoteEvent note = (NoteEvent) event;

                    if(note.isNoteOn() && !isNoteOn[getKeyIndex(note)]) {
                        notesOn++;
                    }
                    else if(!note.isNoteOn() && isNoteOn[getKeyIndex(note)]) {
                        notesOn--;
                    }

                    isNoteOn[getKeyIndex(note)] = note.isNoteOn();

                    if(notesOn > MAX_NOTES_ON) {

                        //Remove the note from the list
                        if(noteToBeRemoved.containsKey(timestamp)) {
                            noteToBeRemoved.get(timestamp).add(note);
                            System.out.println("Removing note at " + timestamp + " because there are too many notes on (" + notesOn + ")");
                        }
                        else {
                            List<NoteEvent> notes = new ArrayList<>();
                            notes.add(note);
                            noteToBeRemoved.put(timestamp, notes);
                        }

                        notesOn--;
                        isNoteOn[getKeyIndex(note)] = false;
                    }

                }

            }
        }

        //Remove the notes
        for(long timestamp : noteToBeRemoved.keySet()) {
            System.out.println("Removing " + noteToBeRemoved.get(timestamp).size() + " notes at " + timestamp);
            sheetMusic.getEventMap().get(timestamp).removeAll(noteToBeRemoved.get(timestamp));
        }
    }

    private int getKeyIndex(NoteEvent key) {
        return key.getKeyNumber() - 21;
    }

}