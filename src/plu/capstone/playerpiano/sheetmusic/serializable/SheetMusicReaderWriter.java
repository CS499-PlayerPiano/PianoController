package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

/**
 * Enum for reading and writing sheet music files.
 * <p>
 *     V1: (Assume all Notes) Notes
 *     V2: (Events) Notes + Tempo
 *     V3: (Events) Notes + Tempo + Sustain Pedal
 *     V4: (Events) Notes + Tempo + Sustain Pedal, short for number of events, and store notes more efficiently
 *     V5: (Events) Notes + Tempo + Sustain Pedal, long for number of events, and store notes more efficiently, int for timestamps
 *     V6: (Events) Notes + Tempo + Sustain Pedal, long for number of events, and store notes more efficiently, int for timestamps, int for song length
 */
@AllArgsConstructor
public enum SheetMusicReaderWriter {

    V1(1, new SheetMusicFileParserV1()),
    V2(2, new SheetMusicFileParserV2()),
    V3(3, new SheetMusicFileParserV3()),
    V4(4, new SheetMusicFileParserV4()),
    V5(5, new SheetMusicFileParserV5()),
    V6(6, new SheetMusicFileParserV6())
    ;

    private final int version;
    private final SheetMusicFileParser fileParser;
    private static final Logger LOGGER = new Logger(SheetMusicReaderWriter.class);

    public static final int LATEST_VERSION;

    public short getVersion() {
        return (short) version;
    }

    static {
        LATEST_VERSION = values()[values().length - 1].version;
        LOGGER.debug("Latest file version: " + LATEST_VERSION);
    }

    public static SheetMusic readSheetMusic(File pianoRollFile) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(pianoRollFile));

        final short version = SheetMusicFileParser.readShort(in);
        SheetMusic sheetMusic = new SheetMusic();

        SheetMusicFileParser fileParser = getByVersion(version);

        if(fileParser != null) {
            sheetMusic = fileParser.readSheetMusic(in);
        }
        else {
            LOGGER.warning("No file parser for version " + version);
        }

        in.close();

        return sheetMusic;
    }

    public static void saveSheetMusic(SheetMusic sheetMusic, File pianoRollFile, int version) throws IOException {
        saveSheetMusic(sheetMusic, pianoRollFile, (short)version);
    }
    public static void saveSheetMusic(SheetMusic sheetMusic, File pianoRollFile, short version) throws IOException {

        BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pianoRollFile.toPath()));

        //version
        SheetMusicFileParser.writeShort(out, version);

        SheetMusicFileParser fileParser = getByVersion(version);

        if (fileParser != null) {
            fileParser.writeSheetMusic(out, sheetMusic);
        } else {
            LOGGER.warning("No file parser for version " + version);
        }

        out.flush();
        out.close();
    }

    private static final SheetMusicFileParser getByVersion(short version) {
        for(SheetMusicReaderWriter parser : values()) {
            if(parser.getVersion() == version) return parser.fileParser;
        }
        return null;
    }
}
