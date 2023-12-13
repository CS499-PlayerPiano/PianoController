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
        sheetMusic.setSongLengthMS(in.readLong(SONG_LENGTH));

        //number of timeslots
        final int numTimeslots = in.readInt(TIMESLOT_COUNT);

        //for each note
        for(int i = 0; i < numTimeslots; ++i) {
            //time
            long time = in.readLong(TIMESLOT);

            //number of events at this time
            int numEventsAtTime = in.readInt(EVENT_COUNT);

            //for each note at this time
            for(int j = 0; j < numEventsAtTime; ++j) {

                final byte eventTypeId = in.readByte(EVENT_TYPE);

                if(eventTypeId == SheetMusicEvent.EVENT_NOTE) {
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
                else if(eventTypeId == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    final int tempo = in.readInt(TEMPO_CHANGE_OBJECT);
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
        out.writeLong(sheetMusic.getSongLengthMS(), SONG_LENGTH);

        //number of timeslots
        out.writeInt(sheetMusic.getEventMap().size(), TIMESLOT_COUNT);

        //for each timeslot
        for(Map.Entry<Long, List<SheetMusicEvent>> entry : sheetMusic.getEventMap().entrySet()) {
            //time
            out.writeLong(entry.getKey(), TIMESLOT);

            //number of events at this time
            out.writeInt(entry.getValue().size(), EVENT_COUNT);

            //for each event at this time
            for(SheetMusicEvent event : entry.getValue()) {

                out.writeByte(event.getEventTypeId(), EVENT_TYPE);

                //write out notes as normal
                if(event.getEventTypeId() == SheetMusicEvent.EVENT_NOTE) {
                    Note note = (Note) event;
                    out.writeByte((byte) note.getKeyNumber(), NOTE_OBJECT);
                    out.writeByte((byte) note.getVelocity(), NOTE_OBJECT);
                    out.writeBoolean(note.isNoteOn(), NOTE_OBJECT);
                    out.writeByte((byte) note.getChannelNum(), NOTE_OBJECT);
                }
                //write out tempo change events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    TempoChangeEvent tempoChangeEvent = (TempoChangeEvent) event;
                    out.writeInt(tempoChangeEvent.getUsPerQuarterNote(), TEMPO_CHANGE_OBJECT);
                }
                else {
                    LOGGER.warning("Unknown event type: " + event.getClass().getName());
                }
            }
        }
    }
}
