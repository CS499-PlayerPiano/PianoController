package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReader;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileWriter;

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
    public SheetMusic readSheetMusic(BufferedPianoFileReader in) throws IOException {
        SheetMusic sheetMusic = new SheetMusic();

        //length in ms
        sheetMusic.setSongLengthMS(in.readLong(SONG_LENGTH));

        //number of timeslots
        final int numTimeslots = in.readInt(TIMESLOT_COUNT);

        //for each note
        for(int i = 0; i < numTimeslots; ++i) {
            //time
            long time = in.readLong(TIMESLOT);

            //number of notes at this time
            int numNotesAtTime = in.readInt(NOTE_COUNT);

            //for each note at this time
            for(int j = 0; j < numNotesAtTime; ++j) {

                final byte keyNumber = in.readByte(NOTE_OBJECT);
                final byte velocity = in.readByte(NOTE_OBJECT);
                final boolean noteOn = in.readBoolean(NOTE_OBJECT);
                final byte channelNum = in.readByte(NOTE_OBJECT);

                Note note = new Note(
                        keyNumber,
                        velocity,
                        noteOn,
                        channelNum
                );

                sheetMusic.putEvent(time, note);
            }
        }

        return sheetMusic;
    }

    @Override
    public void writeSheetMusic(BufferedPianoFileWriter out, SheetMusic sheetMusic) throws IOException {

        //song length
        out.writeLong(sheetMusic.getSongLengthMS(), SONG_LENGTH);

        //number of timeslots
        out.writeInt(sheetMusic.getEventMap().size(), TIMESLOT_COUNT);

        //for each timeslot
        for(Map.Entry<Long, List<SheetMusicEvent>> entry : sheetMusic.getEventMap().entrySet()) {
            //time
            out.writeLong(entry.getKey(), TIMESLOT);

            //We only need to write out the amount of NOTES, not EVENTS!
            int numNotes = 0;
            for(SheetMusicEvent event : entry.getValue()) {
                if(event instanceof Note) {
                    ++numNotes;
                }
            }

            //number of notes at this time
            out.writeInt(numNotes, NOTE_COUNT);

            //for each note at this time
            for(SheetMusicEvent event : entry.getValue()) {

                if(event instanceof Note) {
                    Note note = (Note) event;
                    out.writeByte((byte) note.getKeyNumber(), NOTE_OBJECT);
                    out.writeByte((byte) note.getVelocity(), NOTE_OBJECT);
                    out.writeBoolean(note.isNoteOn(), NOTE_OBJECT);
                    out.writeByte((byte) note.getChannelNum(), NOTE_OBJECT);
                }
            }
        }

    }
}
