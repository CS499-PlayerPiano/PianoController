package plu.capstone.playerpiano.sheetmusic.cleaner.steps;

import plu.capstone.playerpiano.sheetmusic.cleaner.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public class Step2RemoveInvalidChannels implements MidiConversionStep {

    @Override
    public String getName() {
        return "Remove Invalid Channels";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        // Remove all notes that are percussion (only channel 10)
        sheetMusic.getEventMap().values().forEach(listOfEvents -> {
            listOfEvents.removeIf(event -> {
                if(event instanceof NoteEvent) {
                    NoteEvent note = (NoteEvent) event;
                    return note.getChannelNum() == 10;
                }
                return false;
            });
        });

        // Remove empty entries from the event map
        sheetMusic.getEventMap().entrySet().removeIf(entry -> entry.getValue().isEmpty());

    }
}
