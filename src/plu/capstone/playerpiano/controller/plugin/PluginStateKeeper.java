package plu.capstone.playerpiano.controller.plugin;

import plu.capstone.playerpiano.controller.midi.Note;

public abstract class PluginStateKeeper extends Plugin {

    private static final int TOTAL_KEYS = 128;

    private Note[] notes = new Note[TOTAL_KEYS];

    public PluginStateKeeper() {
        super();

        for(int i = 0; i < notes.length; ++i) {
            notes[i] = new Note(
                    (byte) i,
                    (byte) 0,
                    false);
        }
    }

    @Override
    public void onNotePlayed(Note note, long timestamp) {
        final int key = note.getKeyNumber();
        notes[key] = note;
        onNoteChange(notes, timestamp);
    }

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

    public abstract void onNoteChange(Note[] keys, long timestamp);

}
