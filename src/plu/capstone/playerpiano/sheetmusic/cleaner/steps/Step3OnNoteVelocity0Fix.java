package plu.capstone.playerpiano.sheetmusic.cleaner.steps;

import plu.capstone.playerpiano.sheetmusic.cleaner.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

public class Step3OnNoteVelocity0Fix implements MidiConversionStep {

    @Override
    public String getName() {
        return "On note velocity 0 fix";
    }

    @Override
    public void process(SheetMusic sheetMusic) {

        for(long time : sheetMusic.getEventMap().keySet()) {
            for(SheetMusicEvent event : sheetMusic.getEventMap().get(time)) {
                if(event instanceof NoteEvent) {
                    NoteEvent note = (NoteEvent) event;

                    //Note that are on and have a velocity of 0 are actually off notes
                    if(note.getVelocity() == 0 && note.isNoteOn()) {
                        note.setNoteOn(false);
                    }
                }
            }
        }

    }
}
