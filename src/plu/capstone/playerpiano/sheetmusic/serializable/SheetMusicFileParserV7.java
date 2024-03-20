package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;
import plu.capstone.playerpiano.sheetmusic.events.TempoChangeEvent;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReader;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileWriter;

/*
Version 7:
        - int SongLengthMS
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
        if(eventTypeId == EVENT_SUSTAIN_PEDAL)
        - boolean on
*/
public class SheetMusicFileParserV7 extends SheetMusicFileParser {

    @Override
    public SheetMusic readSheetMusic(BufferedPianoFileReader in) throws IOException {
        SheetMusic sheetMusic = new SheetMusic();

        //length in ms
        sheetMusic.setSongLengthMS(in.readInt(SONG_LENGTH));

        //number of timeslots
        final int numTimeslots = in.readInt(TIMESLOT_COUNT);

        //for each note
        for(int i = 0; i < numTimeslots; ++i) {
            //time
            long time = in.readInt(TIMESLOT);

            //number of events at this time
            short numEventsAtTime = in.readShort(EVENT_COUNT);

            //for each note at this time
            for(short j = 0; j < numEventsAtTime; ++j) {

                final byte eventTypeId = in.readByte(EVENT_TYPE);

                //read in notes events
                if(eventTypeId == SheetMusicEvent.EVENT_NOTE) {

                    final boolean noteOn = in.readBoolean(NOTE_OBJECT);
                    final byte keyNumber = in.readByte(NOTE_OBJECT);
                    final byte channelNum = in.readByte(NOTE_OBJECT);

                    byte velocity = 0;

                    if(noteOn) {
                        velocity = in.readByte(NOTE_OBJECT);
                    }

                    Note note = new Note(
                            keyNumber,
                            velocity,
                            noteOn,
                            channelNum
                    );

                    sheetMusic.putEvent(time, note);
                }

                //read in sustain pedal events
                else if(eventTypeId == SheetMusicEvent.EVENT_SUSTAIN_PEDAL) {
                    final boolean on = in.readBoolean(SUSTAIN_PEDAL_OBJECT);
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
    public void writeSheetMusic(BufferedPianoFileWriter out, SheetMusic sheetMusic) throws IOException {
        //song length
        out.writeInt((int)sheetMusic.getSongLengthMS());

        //number of timeslots
        out.writeInt(sheetMusic.getEventMap().size());

        //for each timeslot
        for(Map.Entry<Long, List<SheetMusicEvent>> entry : sheetMusic.getEventMap().entrySet()) {
            //time
            out.writeInt(entry.getKey().intValue()); // Never actually going to be the size of a long, so we can cast to int

            //number of events at this time
            out.writeShort((short) entry.getValue().size());

            //for each event at this time
            for(SheetMusicEvent event : entry.getValue()) {

                out.writeByte(event.getEventTypeId());

                //write out notes as normal
                if(event.getEventTypeId() == SheetMusicEvent.EVENT_NOTE) {
                    Note note = (Note) event;

                    out.writeBoolean(note.isNoteOn());
                    out.writeByte((byte) note.getKeyNumber());
                    out.writeByte((byte) note.getChannelNum());
                    if(note.isNoteOn()) {
                        out.writeByte((byte) note.getVelocity());
                    }


                }

                //write out sustain pedal events as normal
                else if(event.getEventTypeId() == SheetMusicEvent.EVENT_SUSTAIN_PEDAL) {
                    SustainPedalEvent sustainPedalEffect = (SustainPedalEvent) event;
                    out.writeBoolean(sustainPedalEffect.isOn());
                }

                // We don't know what this event is
                else {
                    LOGGER.warning("Unknown event type: " + event.getClass().getName());
                }
            }
        }
    }
}
