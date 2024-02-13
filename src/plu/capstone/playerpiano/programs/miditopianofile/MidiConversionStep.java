package plu.capstone.playerpiano.programs.miditopianofile;

import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public interface MidiConversionStep {

    String getName();
    void process(SheetMusic sheetMusic);

}
