package plu.capstone.playerpiano.sheetmusic;

public interface SheetMusicEvent {

    byte EVENT_NOTE = 0;
    byte EVENT_TEMPO_CHANGE = 1;
    byte EVENT_SUSTAIN_PEDAL = 2;

    byte getEventTypeId();

}
