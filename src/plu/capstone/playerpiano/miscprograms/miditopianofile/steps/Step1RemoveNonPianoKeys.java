package plu.capstone.playerpiano.miscprograms.miditopianofile.steps;

import plu.capstone.playerpiano.miscprograms.miditopianofile.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.Note;
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
                if(event instanceof Note) {
                    Note note = (Note) event;
                    if(!note.isValidPianoKey()) {
                        System.out.println("Removing note: " + note);
                    }
                    return !note.isValidPianoKey();
                }
                return false;
            });
        });

        // Remove empty entries from the event map
        sheetMusic.getEventMap().entrySet().removeIf(entry -> entry.getValue().isEmpty());

    }
}
