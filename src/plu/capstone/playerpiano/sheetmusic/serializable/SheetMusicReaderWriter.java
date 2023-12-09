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

@AllArgsConstructor
public enum SheetMusicReaderWriter {

    V1(1, new SheetMusicFileParserV1()),
    V2(2, new SheetMusicFileParserV2()),
    ;

    private final int version;
    private final SheetMusicFileParser fileParser;
    private static final Logger LOGGER = new Logger(SheetMusicReaderWriter.class);

    public static SheetMusic readSheetMusic(File pianoRollFile) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(pianoRollFile));

        final int version = SheetMusicFileParser.readInt(in);
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

        BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pianoRollFile.toPath()));

        //version
        SheetMusicFileParser.writeInt(out, version);

        SheetMusicFileParser fileParser = getByVersion(version);

        if (fileParser != null) {
            fileParser.writeSheetMusic(out, sheetMusic);
        } else {
            LOGGER.warning("No file parser for version " + version);
        }

        out.flush();
        out.close();
    }

    private static final SheetMusicFileParser getByVersion(int version) {
        for(SheetMusicReaderWriter parser : values()) {
            if(parser.version == version) return parser.fileParser;
        }
        return null;
    }
}
