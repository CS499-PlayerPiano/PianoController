package plu.capstone.playerpiano.controller.plugin;

import plu.capstone.playerpiano.controller.midi.Note;

public abstract class PluginStateKeeper extends Plugin {

    private static final int TOTAL_KEYS = 88;

    private boolean[] keys = new boolean[TOTAL_KEYS];
    private int[] velocities = new int[TOTAL_KEYS];

    private Note[] notes = new Note[TOTAL_KEYS];

    @Override
    public void onEnable() {
        for(int i = 0; i < keys.length; ++i) {
            keys[i] = false;
            velocities[i] = 0;
        }

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
        if(key > TOTAL_KEYS - 1) {
            return;
        }
        keys[key] = note.isNoteOn();
        velocities[key] = note.getVelocity();
        notes[key] = note;
        onNoteChange(keys, velocities);
        onNoteChange2(notes);
    }

    @Override
    public void onSongFinished() {
        for(int i = 0; i < keys.length; ++i) {
            keys[i] = false;
            velocities[i] = 0;
        }

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

        onNoteChange(keys, velocities);
        onNoteChange2(notes);
    }

    public abstract void onNoteChange(boolean[] keys, int[] velocities);
    public abstract void onNoteChange2(Note[] keys);

}
