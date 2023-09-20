package plu.capstone.playerpiano.controller.midi;

public interface NoteCallback {

    public static final long LIVE_TIMESTAMP = -1;

    void onNotePlayed(Note note, long timestamp);

    default long getOffset() {
        return 0;
    }

    default void onSongStarted() {}
    default void onSongFinished() {}
}
