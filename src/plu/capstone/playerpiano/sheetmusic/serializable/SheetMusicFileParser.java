package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReader;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileWriter;

public abstract class SheetMusicFileParser {

    protected final Logger LOGGER = new Logger(new Logger(SheetMusicFileParser.class), this.getClass().getSimpleName());

    public abstract SheetMusic readSheetMusic(BufferedPianoFileReader in) throws IOException;
    public abstract void writeSheetMusic(BufferedPianoFileWriter out, SheetMusic sheetMusic) throws IOException;

    
    protected static final String SONG_LENGTH = "Song Length";
    protected static final String TIMESLOT_COUNT = "Timeslot Count";
    protected static final String TIMESLOT = "Time an event occurs";
    protected static final String NOTE_COUNT = "Number of notes at this time";
    protected static final String EVENT_COUNT = "Number of events at this time";
    protected static final String EVENT_TYPE = "Event Type";
    protected static final String NOTE_OBJECT = "Note Object";
    protected static final String TEMPO_CHANGE_OBJECT = "Event Object";
    protected static final String SUSTAIN_PEDAL_OBJECT = "Sustain Pedal Object";
}
