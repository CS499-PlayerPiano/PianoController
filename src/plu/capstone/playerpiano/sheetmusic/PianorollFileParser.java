package plu.capstone.playerpiano.sheetmusic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParserV1;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParserV2;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileWriter;

/*
        Version 1:
        - long SongLengthMS
        - int number of timeslots
        - for each timeslot:
            - long time
            - int number of notes at this time
            - for each note at this time:
                - byte keyNumber
                - byte velocity
                - boolean noteOn
                - byte channelNum

Version 2:
- long SongLengthMS
- int number of timeslots
- for each timeslot:
    - long time
    - int number of events at this time
    - for each event at this time:
        - byte eventTypeId
        if(eventTypeId == EVENT_NOTE)
            - byte keyNumber
            - byte velocity
            - boolean noteOn
            - byte channelNum
 */
public class PianorollFileParser {

    private static final Logger LOGGER = new Logger(PianorollFileParser.class);

    @AllArgsConstructor
    enum EnumFileParser {

        V1(new SheetMusicFileParserV1()),
        V2(new SheetMusicFileParserV2()),
        ;

        private SheetMusicFileWriter fileParser;

        public SheetMusicFileWriter get() {return fileParser;}
    }

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
        origv2.putNote(6, new Note(
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
        translateSheetMusicToPiannoroll(origv1, file, 1);
        SheetMusic newSheetMusicv1 = translatePianorollToSheetMusic(file);
        verifySheetMusic(origv1, newSheetMusicv1, "v1");

        file = new File("tmp/v2.pianoroll");
        translateSheetMusicToPiannoroll(origv2, file, 2);
        SheetMusic newSheetMusicv2 = translatePianorollToSheetMusic(file);
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

    public static SheetMusic translatePianorollToSheetMusic(File pianoRollFile) throws IOException {

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(pianoRollFile));

        final int version = readInt(in);
        SheetMusic sheetMusic = new SheetMusic();

        /*
        Version 1:
         */
        if(version == 1) {
            sheetMusic = EnumFileParser.V1.get().readSheetMusic(in);
        }
        else if(version == 2) {
            sheetMusic = EnumFileParser.V2.get().readSheetMusic(in);
        }
        else {
            LOGGER.error("Error reading file. Unknown version: " + version);
        }

        in.close();

        return sheetMusic;
    }

    public static void translateSheetMusicToPiannoroll(SheetMusic sheetMusic, File pianoRollFile, int version) throws IOException {

        BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pianoRollFile.toPath()));

        //version
        writeInt(out, version);

        if(version == 1) {
            EnumFileParser.V1.get().writeSheetMusic(out, sheetMusic);
        }
        else if(version == 2) {

            EnumFileParser.V2.get().writeSheetMusic(out, sheetMusic);

        }
        else {
            LOGGER.error("Error writing file. Unknown version: " + version);
        }

        out.flush();
        out.close();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void writeInt(BufferedOutputStream out, int num) throws IOException {
        out.write(num >> 24);
        out.write(num >> 16);
        out.write(num >> 8);
        out.write(num);
    }

    private static int readInt(BufferedInputStream in) throws IOException {
        int num = 0;
        num |= in.read() << 24;
        num |= in.read() << 16;
        num |= in.read() << 8;
        num |= in.read();
        return num;
    }

}
