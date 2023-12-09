package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.TempoChangeEvent;

public class SheetMusicFileParserV2 extends SheetMusicFileWriter {

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

            //number of events at this time
            int numEventsAtTime = readInt(in);

            //for each note at this time
            for(int j = 0; j < numEventsAtTime; ++j) {

                final byte eventTypeId = readByte(in);

                if(eventTypeId == SheetMusicEvent.EVENT_NOTE) {
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
                else if(eventTypeId == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    final int tempo = readInt(in);
                    sheetMusic.putEvent(time, new TempoChangeEvent(tempo));
                }
                else {
                    LOGGER.warning("Unknown event type: " + eventTypeId);
                }

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

            //number of events at this time
            writeInt(out, entry.getValue().size());

            //for each event at this time
            for(SheetMusicEvent event : entry.getValue()) {

                writeByte(out, event.getEventTypeId());

                //write out notes as normal
                if(event.getEventTypeId() == SheetMusicEvent.EVENT_NOTE) {
                    Note note = (Note) event;
                    writeByte(out, (byte) note.getKeyNumber());
                    writeByte(out, (byte) note.getVelocity());
                    writeBoolean(out, note.isNoteOn());
                    writeByte(out, (byte) note.getChannelNum());
                }
                //write out tempo change events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    TempoChangeEvent tempoChangeEvent = (TempoChangeEvent) event;
                    writeInt(out, tempoChangeEvent.getTempo());
                }
                else {
                    LOGGER.warning("Unknown event type: " + event.getClass().getName());
                }
            }
        }
    }
}
