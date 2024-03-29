package plu.capstone.playerpiano.sheetmusic.cleaner.steps;

import plu.capstone.playerpiano.sheetmusic.cleaner.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public class Step1RemoveNonPianoKeys implements MidiConversionStep {

    @Override
    public String getName() {
        return "Remove Non Piano Keys";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        // Remove all notes that are not valid piano keys
        sheetMusic.getEventMap().values().forEach(listOfEvents -> {
            listOfEvents.removeIf(event -> {
                if(event instanceof NoteEvent) {
                    NoteEvent note = (NoteEvent) event;
                    return !isValidPianoKey(note);
                }
                return false;
            });
        });

        // Remove empty entries from the event map
        sheetMusic.getEventMap().entrySet().removeIf(entry -> entry.getValue().isEmpty());

    }

    private static final boolean isValidPianoKey(NoteEvent note) {
        return note.getKeyNumber() >= 21 && note.getKeyNumber() <= 108;
    }
}
