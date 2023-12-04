package plu.capstone.playerpiano.sheetmusic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SheetMusicFileFormatIdea {

    int version;
    Header header;


    class Header {
        int noteCount;
        long songLengthMS;
        int chunks;
    }


    /*
    There needs to be a way we can "chunk" music, and stream chunks
    of data to HashMaps, so we don't load the entire thing into memory.

    Reading blackmidis into Hashmaps I believe is why the slowdown during playback offurrs
    due to all the lookups.

     1 million key value pairs, each value having a max of 9000 objects in it

     */
    class Music {
        HashMap<Long, List<Note>> sheetMusic;
        List<Map<Long, List<Note>>> chunksOfSheetMusic;

        Queue<TimestampedEntry> queue;
    }

    class TimestampedEntry {
        long timestamp;
        List<Note> notes;
    }

}
