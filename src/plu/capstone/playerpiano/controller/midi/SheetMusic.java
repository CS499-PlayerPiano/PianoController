package plu.capstone.playerpiano.controller.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import plu.capstone.playerpiano.controller.logger.Logger;

/**
 * Base class for any type of sheet music.
 * Can be extended, or used as is.
 */
public class SheetMusic {

    private final Logger logger = new Logger(this);

    @Getter(value = lombok.AccessLevel.PROTECTED)
    private Map<Long, List<Note>> noteMap = new HashMap<>();

    @Getter
    protected long songLengthMS = 0;

    @Getter
    private boolean isPlaying = false;
    private List<NoteCallback> callbacks = new ArrayList<>();

    /**
     * Creates a new SheetMusic object.
     * @param time The time in milliseconds that this note starts.
     * @param note The note to add.
     */
    public final void putNote(long time, Note note) {
        if(!noteMap.containsKey(time)) {
            noteMap.put(time, new ArrayList<>());
        }
        noteMap.get(time).add(note);
    }

    /**
     * Adds a callback to this SheetMusic object.
     * @param callback The callback to add.
     */
    public final void addCallback(NoteCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Starts playing this SheetMusic object.
     */
    public void play() {
        if(isPlaying) {throw new IllegalStateException("Already playing");}
        this.isPlaying = true;

        for(NoteCallback callback : callbacks) {
            callback.onSongStarted(0, noteMap);
        }

        final long startTime = System.nanoTime();
        long prevTime = startTime - 1_000_000;

        while(isPlaying) {
            long time = (System.nanoTime() - startTime) / 1_000_000;

            if(time > songLengthMS) {
                stop();
                return;
            }

            if(time == prevTime) {
                try {
                    Thread.sleep(0, 500_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                while(time > prevTime) {
                    //System.out.println("Time: " + time + " PrevTime: " + prevTime);
                    ++prevTime;

                    for(NoteCallback callback : callbacks) {
                        final long newTime = prevTime + callback.getOffset();
                        List<Note> msgs = noteMap.getOrDefault(newTime, null);
                        if(msgs != null) {
                            callback.onNotesPlayed(msgs.toArray(new Note[0]), newTime);
                        }
                    }
                }
            }

            prevTime = time;

        }

        for(NoteCallback callback : callbacks) {
            callback.onSongFinished(prevTime);
        }

    }

    /**
     * Stops playing this SheetMusic object.
     */
    public void stop() {
        this.isPlaying = false;
    }

}
