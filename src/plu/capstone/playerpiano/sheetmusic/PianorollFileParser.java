package plu.capstone.playerpiano.sheetmusic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParserV1;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParserV2;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParser;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;


public class PianorollFileParser {

    private static final Logger LOGGER = new Logger(PianorollFileParser.class);

    //Test read and write
    public static void main(String[] args) throws IOException {
        SheetMusic origv1 = new SheetMusic();
        SheetMusic origv2 = new SheetMusic();
        origv1.songLengthMS = 1000;
        origv1.putNote(0, new Note(
                (byte) 68,
                (byte) 128,
                true,
                1
        ));
        origv1.putNote(6, new Note(
                (byte) 68,
                (byte) 0,
                false,
                1
        ));

        origv2.songLengthMS = 1000;
        origv2.putNote(0, new Note(
                (byte) 68,
                (byte) 128,
                true,
                1
        ));
        origv2.putNote(6, new Note(
                (byte) 68,
                (byte) 0,
                false,
                1
        ));

        origv2.putEvent(23, new TempoChangeEvent(120));

        File file = new File("tmp/v1.pianoroll");
        SheetMusicReaderWriter.saveSheetMusic(origv1, file, 1);
        SheetMusic newSheetMusicv1 = SheetMusicReaderWriter.readSheetMusic(file);
        verifySheetMusic(origv1, newSheetMusicv1, "v1");

        file = new File("tmp/v2.pianoroll");
        SheetMusicReaderWriter.saveSheetMusic(origv2, file, 2);
        SheetMusic newSheetMusicv2 = SheetMusicReaderWriter.readSheetMusic(file);
        verifySheetMusic(origv2, newSheetMusicv2, "v2");
    }

    private static void verifySheetMusic(SheetMusic orig, SheetMusic newSheetMusic, String version) {
        boolean success = orig.equals(newSheetMusic);
        LOGGER.info("Verifying " + version + "...");
        if(!success) {
            LOGGER.error("Failed!");
            LOGGER.debug("songLengthMS: " + (orig.songLengthMS == newSheetMusic.songLengthMS) + " " + orig.songLengthMS + " " + newSheetMusic.songLengthMS);
            LOGGER.debug("eventMapSize: " + (orig.getEventMap().size() == newSheetMusic.getEventMap().size()) + " " + orig.getEventMap().size() + " " + newSheetMusic.getEventMap().size());
            LOGGER.debug("eventMap: " + (orig.getEventMap().equals(newSheetMusic.getEventMap())));

            for(long key : orig.getEventMap().keySet()) {
                List<SheetMusicEvent> origEvents = orig.getEventMap().get(key);
                List<SheetMusicEvent> newEvents = newSheetMusic.getEventMap().get(key);
                LOGGER.debug("key: " + key);
                LOGGER.debug("origEvents: " + origEvents);
                LOGGER.debug("newEvents: " + newEvents);
                LOGGER.debug("origEventsSize: " + origEvents.size());
                LOGGER.debug("newEventsSize: " + newEvents.size());
                LOGGER.debug("origEventsEquals: " + origEvents.equals(newEvents));
                for(int i = 0; i < origEvents.size(); ++i) {
                    SheetMusicEvent origEvent = origEvents.get(i);
                    SheetMusicEvent newEvent = newEvents.get(i);
                    LOGGER.debug("origEvent: " + origEvent);
                    LOGGER.debug("newEvent: " + newEvent);
                    LOGGER.debug("origEventEquals: " + origEvent.equals(newEvent));
                }
            }

        }
        else {
            LOGGER.info("Success!");
        }
    }

}
