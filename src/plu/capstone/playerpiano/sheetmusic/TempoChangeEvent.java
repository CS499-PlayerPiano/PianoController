package plu.capstone.playerpiano.sheetmusic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TempoChangeEvent implements SheetMusicEvent {

    private int tempo;

    @Override
    public byte getEventTypeId() {
        return EVENT_TEMPO_CHANGE;
    }

    @Override
    public String toString() {
        return "TimeChangeEvent{" +
                "tempo=" + tempo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TempoChangeEvent that = (TempoChangeEvent) o;

        return tempo == that.tempo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(tempo);
    }
}
