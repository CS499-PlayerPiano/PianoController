package plu.capstone.playerpiano.miscprograms.miditopianofile.steps;

import plu.capstone.playerpiano.miscprograms.miditopianofile.MidiConversionStep;
import plu.capstone.playerpiano.sheetmusic.events.Note;
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
                if(event instanceof Note) {
                    Note note = (Note) event;

                    //Note that are on and have a velocity of 0 are actually off notes
                    if(note.getVelocity() == 0 && note.isNoteOn()) {
                        note.setNoteOn(false);
                    }
                }
            }
        }

    }
}
