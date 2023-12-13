package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.TempoChangeEvent;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReader;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileWriter;

/*
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
        if(eventTypeId == EVENT_TEMPO_CHANGE)
        - int tempo
*/
public class SheetMusicFileParserV2 extends SheetMusicFileParser {

    @Override
    public SheetMusic readSheetMusic(BufferedPianoFileReader in) throws IOException {
        SheetMusic sheetMusic = new SheetMusic();

        //length in ms
        sheetMusic.setSongLengthMS(in.readLong());

        //number of timeslots
        final int numTimeslots = in.readInt();

        //for each note
        for(int i = 0; i < numTimeslots; ++i) {
            //time
            long time = in.readLong();

            //number of events at this time
            int numEventsAtTime = in.readInt();

            //for each note at this time
            for(int j = 0; j < numEventsAtTime; ++j) {

                final byte eventTypeId = in.readByte();

                if(eventTypeId == SheetMusicEvent.EVENT_NOTE) {
                    final byte keyNumber = in.readByte();
                    final byte velocity = in.readByte();
                    final boolean noteOn = in.readBoolean();
                    final byte channelNum = in.readByte();

                    Note note = new Note(
                            keyNumber,
                            velocity,
                            noteOn,
                            channelNum
                    );

                    sheetMusic.putEvent(time, note);
                }
                else if(eventTypeId == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    final int tempo = in.readInt();
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
    public void writeSheetMusic(BufferedPianoFileWriter out, SheetMusic sheetMusic) throws IOException {
        //song length
        out.writeLong(sheetMusic.getSongLengthMS());

        //number of timeslots
        out.writeInt(sheetMusic.getEventMap().size());

        //for each timeslot
        for(Map.Entry<Long, List<SheetMusicEvent>> entry : sheetMusic.getEventMap().entrySet()) {
            //time
            out.writeLong(entry.getKey());

            //number of events at this time
            out.writeInt(entry.getValue().size());

            //for each event at this time
            for(SheetMusicEvent event : entry.getValue()) {

                out.writeByte(event.getEventTypeId());

                //write out notes as normal
                if(event.getEventTypeId() == SheetMusicEvent.EVENT_NOTE) {
                    Note note = (Note) event;
                    out.writeByte((byte) note.getKeyNumber());
                    out.writeByte((byte) note.getVelocity());
                    out.writeBoolean(note.isNoteOn());
                    out.writeByte((byte) note.getChannelNum());
                }
                //write out tempo change events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    TempoChangeEvent tempoChangeEvent = (TempoChangeEvent) event;
                    out.writeInt(tempoChangeEvent.getUsPerQuarterNote());
                }
                else {
                    LOGGER.warning("Unknown event type: " + event.getClass().getName());
                }
            }
        }
    }
}
