package plu.capstone.playerpiano.sheetmusic;

import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.sheetmusic.Note;

/**
 * This is the callback interface for the {@link plu.capstone.playerpiano.sheetmusic.SheetMusic} class.
 */
public interface NoteCallback {

    /**
     * This is the timestamp for live events. This is used when a note or event is played live.
     */
    long LIVE_TIMESTAMP = -1;

    /**
     * Called when X note(s) are played / state changed at a given timestamp.
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    void onNotesPlayed(Note[] notes, long timestamp);

    /**
     * Returns the offset of the song in milliseconds. This is used to calculate the timestamp of the event.
     * By default, this is 0. If the offset is negative, we will call {@link #onNotesPlayed(plu.capstone.playerpiano.sheetmusic.Note[], long)} of the timestamp minus the offset.
     *
     * @return offset of the callback in milliseconds.
     */
    default long getOffset() { return 0; }

    /**
     * Called when the song starts. This is called before the first note is played.
     * @param timestamp This will always be 0, but is included for consistency and backwards compatibility.
     * @param entireNoteMap This is a map of all the notes in the song. The key is the timestamp of the note.
     * It should be noted that {@link #getOffset()} has not been applied to the timestamps.
     */
    default void onSongStarted(long timestamp, Map<Long, List<Note>> entireNoteMap) {}

    /**
     * Called when the song finishes. This is called after the last note is played.
     * @param timestamp This will always be the length of the song in milliseconds,
     *                  but is included for consistency and backwards compatibility.
     */
    default void onSongFinished(long timestamp) {}

    /**
     * Called every millisecond while the song is playing.
     * @param current The current timestamp in milliseconds.
     * @param end The timestamp of the end of the song in milliseconds.
     */
    default void onTimestamp(long current, long end) {}
}
