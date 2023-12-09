package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicEvent;

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
*/
public class SheetMusicFileParserV1 extends SheetMusicFileParser {

    @Override
    public SheetMusic readSheetMusic(BufferedInputStream in) throws IOException {
        SheetMusic sheetMusic = new SheetMusic();

        //length in ms
        sheetMusic.setSongLengthMS(readLong(in));

        //number of timeslots
        final int numTimeslots = readInt(in);

        //for each note
        for(int i = 0; i < numTimeslots; ++i) {
            //time
            long time = readLong(in);

            //number of notes at this time
            int numNotesAtTime = readInt(in);

            //for each note at this time
            for(int j = 0; j < numNotesAtTime; ++j) {

                final byte keyNumber = readByte(in);
                final byte velocity = readByte(in);
                final boolean noteOn = readBoolean(in);
                final byte channelNum = readByte(in);

                Note note = new Note(
                        keyNumber,
                        velocity,
                        noteOn,
                        channelNum
                );

                sheetMusic.putNote(time, note);
            }
        }

        return sheetMusic;
    }

    @Override
    public void writeSheetMusic(BufferedOutputStream out, SheetMusic sheetMusic) throws IOException {

        //song length
        writeLong(out, sheetMusic.getSongLengthMS());

        //number of timeslots
        writeInt(out, sheetMusic.getEventMap().size());

        //for each timeslot
        for(Map.Entry<Long, List<SheetMusicEvent>> entry : sheetMusic.getEventMap().entrySet()) {
            //time
            writeLong(out, entry.getKey());

            //We only need to write out the amount of NOTES, not EVENTS!
            int numNotes = 0;
            for(SheetMusicEvent event : entry.getValue()) {
                if(event instanceof Note) {
                    ++numNotes;
                }
            }

            //number of notes at this time
            writeInt(out, numNotes);

            //for each note at this time
            for(SheetMusicEvent event : entry.getValue()) {

                if(event instanceof Note) {
                    Note note = (Note) event;
                    writeByte(out, (byte) note.getKeyNumber());
                    writeByte(out, (byte) note.getVelocity());
                    writeBoolean(out, note.isNoteOn());
                    writeByte(out, (byte) note.getChannelNum());
                }
            }
        }

    }
}
