package plu.capstone.playerpiano.controller.midi;

import java.util.List;
import java.util.Map;

public interface NoteCallback {

    public static final long LIVE_TIMESTAMP = -1;

    void onNotePlayed(Note note, long timestamp);

    default long getOffset() {
        return 0;
    }

    default void onSongStarted(long timestamp, Map<Long, List<Note>> entireNoteMap) {}
    default void onSongFinished(long timestamp) {}
}
