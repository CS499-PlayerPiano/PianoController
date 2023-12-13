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

}
