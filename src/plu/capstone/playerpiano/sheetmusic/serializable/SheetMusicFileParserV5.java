package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;
import plu.capstone.playerpiano.sheetmusic.events.TempoChangeEvent;

/*
Version 5:
        - long SongLengthMS
        - int number of timeslots
        - for each timeslot:
        - int time
        - short number of events at this time
        - for each event at this time:
        - byte eventTypeId
        if(eventTypeId == EVENT_NOTE)
        - boolean noteOn
        - byte keyNumber
        - byte channelNum
        if note on
            - byte velocity
        if(eventTypeId == EVENT_TEMPO_CHANGE)
        - int tempo
        if(eventTypeId == EVENT_SUSTAIN_PEDAL)
        - boolean on
*/
public class SheetMusicFileParserV5 extends SheetMusicFileParser {

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
            long time = readInt(in);

            //number of events at this time
            short numEventsAtTime = readShort(in);

            //for each note at this time
            for(short j = 0; j < numEventsAtTime; ++j) {

                final byte eventTypeId = readByte(in);

                //read in notes events
                if(eventTypeId == SheetMusicEvent.EVENT_NOTE) {

                    final boolean noteOn = readBoolean(in);
                    final byte keyNumber = readByte(in);
                    final byte channelNum = readByte(in);

                    byte velocity = 0;

                    if(noteOn) {
                        velocity = readByte(in);
                    }

                    Note note = new Note(
                            keyNumber,
                            velocity,
                            noteOn,
                            channelNum
                    );

                    sheetMusic.putEvent(time, note);
                }

                //read in tempo change events
                else if(eventTypeId == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    final int tempo = readInt(in);
                    sheetMusic.putEvent(time, new TempoChangeEvent(tempo));
                }

                //read in sustain pedal events
                else if(eventTypeId == SheetMusicEvent.EVENT_SUSTAIN_PEDAL) {
                    final boolean on = readBoolean(in);
                    sheetMusic.putEvent(time, new SustainPedalEvent(on));
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
            writeInt(out, entry.getKey().intValue()); // Never actually going to be the size of a long, so we can cast to int

            //number of events at this time
            writeShort(out, (short) entry.getValue().size());

            //for each event at this time
            for(SheetMusicEvent event : entry.getValue()) {

                writeByte(out, event.getEventTypeId());

                //write out notes as normal
                if(event.getEventTypeId() == SheetMusicEvent.EVENT_NOTE) {
                    Note note = (Note) event;

                    writeBoolean(out, note.isNoteOn());
                    writeByte(out, (byte) note.getKeyNumber());
                    writeByte(out, (byte) note.getChannelNum());
                    if(note.isNoteOn()) {
                        writeByte(out, (byte) note.getVelocity());
                    }


                }

                //write out tempo change events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_TEMPO_CHANGE) {
                    TempoChangeEvent tempoChangeEvent = (TempoChangeEvent) event;
                    writeInt(out, tempoChangeEvent.getUsPerQuarterNote());
                }

                //write out sustain pedal events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_SUSTAIN_PEDAL) {
                    SustainPedalEvent sustainPedalEffect = (SustainPedalEvent) event;
                    writeBoolean(out, sustainPedalEffect.isOn());
                }

                // We don't know what this event is
                else {
                    LOGGER.warning("Unknown event type: " + event.getClass().getName());
                }
            }
        }
    }
}
