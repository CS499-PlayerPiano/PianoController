package plu.capstone.playerpiano.outputs;

import java.util.List;
import plu.capstone.playerpiano.sheetmusic.events.Note;

/**
 * An extension of the Output class that keeps track of the state of the piano notes.
 */
public abstract class OutputStateKeeper extends Output {

    private static final int TOTAL_KEYS = 128;

    private Note[] notes = new Note[TOTAL_KEYS];

    /**
     * Creates a new OutputStateKeeper.
     */
    public OutputStateKeeper() {
        super();

        for(int i = 0; i < notes.length; ++i) {
            notes[i] = new Note(
                    (byte) i,
                    (byte) 0,
                    false);
        }
    }

    /**
     * Called when X note(s) are played / state changed at a given timestamp.
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    @Override
    public final void onNotesPlayed(List<Note> notes, long timestamp) {
        for(Note note : notes) {
            final int key = note.getKeyNumber();
            this.notes[key] = note;
        }
        onNoteChange(this.notes, timestamp);
    }

    /**
     * Called when the song finishes. This is called after the last note is played.
     * @param timestamp This will always be the length of the song in milliseconds,
     *                  but is included for consistency and backwards compatibility.
     *
     * If you override this method, you must call super.onSongFinished(timestamp) at the end of your method!
     */
    @Override
    public void onSongFinished(long timestamp) {

        for(int i = 0; i < notes.length; ++i) {
            final Note oldNote = notes[i];
            if(oldNote != null) {
                final Note newNote = new Note(
                        (byte) oldNote.getKeyNumber(),
                        (byte) oldNote.getVelocity(),
                        false
                );
                notes[i] = newNote;
            }
        }

        onNoteChange(notes, timestamp);
    }

    /**
     * Called when the state of the notes changes.
     * @param keys The new state of the keys.
     * @param timestamp The timestamp of the event in milliseconds. If this is a live event, this will be {@link plu.capstone.playerpiano.sheetmusic.SheetMusicCallback#LIVE_TIMESTAMP}
     */
    public abstract void onNoteChange(Note[] keys, long timestamp);

}
