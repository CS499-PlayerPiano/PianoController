package plu.capstone.playerpiano.miscprograms.miditopianofile;

import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public interface MidiConversionStep {

    String getName();
    void process(SheetMusic sheetMusic);

}
