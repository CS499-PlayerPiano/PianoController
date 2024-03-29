package plu.capstone.playerpiano.outputs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

@AllArgsConstructor
@Getter
public class TimedEvents {
    private final long timestamp;
    private final List<SheetMusicEvent> events;
}
