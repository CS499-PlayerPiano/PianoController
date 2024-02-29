package plu.capstone.playerpiano.sheetmusic.events;

public interface SheetMusicEvent {

    byte EVENT_NOTE = 0;

    @Deprecated
    byte EVENT_TEMPO_CHANGE = 1;


    byte EVENT_SUSTAIN_PEDAL = 2;

    byte getEventTypeId();

}
