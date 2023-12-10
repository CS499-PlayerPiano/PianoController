package test;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.TempoChangeEvent;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

class SheetMusicReaderWriterTest {

    @Test
    public void testV1ReadWrite() throws IOException {
        SheetMusic orig = new SheetMusic();
        orig.setSongLengthMS(1000);
        orig.putEvent(0, new Note(
                (byte) 68,
                (byte) 128,
                true,
                1
        ));
        orig.putEvent(6, new Note(
                (byte) 68,
                (byte) 0,
                false,
                1
        ));

        File file = new File("tmp/v1.pianoroll");
        SheetMusicReaderWriter.saveSheetMusic(orig, file, 1);
        SheetMusic newSheetMusic = SheetMusicReaderWriter.readSheetMusic(file);


        assertEquals(orig, newSheetMusic);
        assertEquals(orig.getSongLengthMS(), newSheetMusic.getSongLengthMS());
        assertEquals(orig.getEventMap().size(), newSheetMusic.getEventMap().size());
        assertEquals(orig.getEventMap(), newSheetMusic.getEventMap());
    }

    @Test
    public void testV2ReadWrite() throws IOException {
        SheetMusic orig = new SheetMusic();
        orig.setSongLengthMS(1000);
        orig.putEvent(0, new Note(
                (byte) 68,
                (byte) 128,
                true,
                1
        ));
        orig.putEvent(6, new Note(
                (byte) 68,
                (byte) 0,
                false,
                1
        ));

        orig.putEvent(23, new TempoChangeEvent(120));

        File file = new File("tmp/v2.pianoroll");
        SheetMusicReaderWriter.saveSheetMusic(orig, file, 2);
        SheetMusic newSheetMusic = SheetMusicReaderWriter.readSheetMusic(file);


        assertEquals(orig, newSheetMusic);
        assertEquals(orig.getSongLengthMS(), newSheetMusic.getSongLengthMS());
        assertEquals(orig.getEventMap().size(), newSheetMusic.getEventMap().size());
        assertEquals(orig.getEventMap(), newSheetMusic.getEventMap());
    }

}