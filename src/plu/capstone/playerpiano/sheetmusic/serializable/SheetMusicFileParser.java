package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReader;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileWriter;

public abstract class SheetMusicFileParser {

    protected final Logger LOGGER = new Logger(new Logger(SheetMusicFileParser.class), this.getClass().getSimpleName());

    public abstract SheetMusic readSheetMusic(BufferedPianoFileReader in) throws IOException;
    public abstract void writeSheetMusic(BufferedPianoFileWriter out, SheetMusic sheetMusic) throws IOException;

    public static final WhatIsItFor VERSION = new WhatIsItFor("Version", 0xFFB6C1);
    public static final WhatIsItFor SONG_LENGTH = new WhatIsItFor("Song Length", 0xADD8E6);
    public static final WhatIsItFor TIMESLOT_COUNT = new WhatIsItFor("Timeslot Count", 0x90EE90);
    public static final WhatIsItFor TIMESLOT = new WhatIsItFor("Time an event occurs", 0xFFD700);
    public static final WhatIsItFor NOTE_COUNT = new WhatIsItFor("Number of notes at this time", 0xFFA500);
    public static final WhatIsItFor EVENT_COUNT = new WhatIsItFor("Number of events at this time", 0xFFA500);
    public static final WhatIsItFor EVENT_TYPE = new WhatIsItFor("Event Type", 0xFF69B4);
    public static final WhatIsItFor NOTE_OBJECT = new WhatIsItFor("Note Object", 0xB10DC9);
    public static final WhatIsItFor TEMPO_CHANGE_OBJECT = new WhatIsItFor("Event Object", 0x40E0D0);
    public static final WhatIsItFor SUSTAIN_PEDAL_OBJECT = new WhatIsItFor("Sustain Pedal Object", 0xD2B48C);

    @AllArgsConstructor
    @Getter
    public static class WhatIsItFor {
        private final String name;
        private final int hexColor;
    }
}
