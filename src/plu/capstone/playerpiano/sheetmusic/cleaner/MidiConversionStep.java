package plu.capstone.playerpiano.sheetmusic.cleaner;

import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public interface MidiConversionStep {

    String getName();
    void process(SheetMusic sheetMusic);

}
