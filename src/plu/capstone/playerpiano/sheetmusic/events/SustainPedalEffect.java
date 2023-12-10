package plu.capstone.playerpiano.sheetmusic.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SustainPedalEffect implements SheetMusicEvent{

    private final boolean on;

    @Override
    public byte getEventTypeId() {
        return EVENT_SUSTAIN_PEDAL;
    }

    @Override
    public String toString() {
        return "SustainPedalEffect{" +
                "on=" + on +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SustainPedalEffect that = (SustainPedalEffect) o;

        return on == that.on;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(on);
    }
}
