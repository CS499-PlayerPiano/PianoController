package plu.capstone.playerpiano.midiconverter2;

import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public interface MidiConversionStep {

    String getName();
    void process(SheetMusic sheetMusic);

}
