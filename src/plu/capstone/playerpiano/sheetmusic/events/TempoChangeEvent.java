package plu.capstone.playerpiano.sheetmusic.events;

import lombok.Getter;

@Getter
@Deprecated
public class TempoChangeEvent implements SheetMusicEvent {

    private final int usPerQuarterNote;
    private final int BPM;

    public TempoChangeEvent(int tempo) {
        this.usPerQuarterNote = tempo;
        this.BPM = 60000000 / tempo;
    }

    @Override
    public byte getEventTypeId() {
        return EVENT_TEMPO_CHANGE;
    }

    @Override
    public String toString() {
        return "TimeChangeEvent{" +
                "us_per_quarter_note=" + usPerQuarterNote +
                ", BPM=" + BPM +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TempoChangeEvent that = (TempoChangeEvent) o;

        //BPM is not included in equals because it is derived from tempo
        return usPerQuarterNote == that.usPerQuarterNote;
    }

    //BPM is not included in hashCode because it is derived from tempo
    @Override
    public int hashCode() {
        return Integer.hashCode(usPerQuarterNote);
    }
}
