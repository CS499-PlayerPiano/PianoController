package plu.capstone.playerpiano.sheetmusic.events;

public interface SheetMusicEvent {

    byte EVENT_NOTE = 0;

    byte EVENT_SUSTAIN_PEDAL = 1;

    byte getEventTypeId();

}
