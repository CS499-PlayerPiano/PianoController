package plu.capstone.playerpiano.sheetmusic;

import java.util.List;
import java.util.Map;

public class SheetMusicFileFormatIdea {

    int version;
    Header header;
    Map<Integer, List<Note>> notes;


    class Header {
        int noteCount;
        long songLengthMS;
    }


}
