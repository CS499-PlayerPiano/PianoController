package plu.capstone.playerpiano.controller.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import plu.capstone.playerpiano.controller.logger.Logger;

public class SheetMusic {

    private final Logger logger = new Logger(this);

    @Getter(value = lombok.AccessLevel.PROTECTED)
    private Map<Long, List<Note>> noteMap = new HashMap<>();

    @Getter
    protected long songLengthMS = 0;

    @Getter
    private boolean isPlaying = false;
    private List<NoteCallback> callbacks = new ArrayList<>();

    public final void putNote(long time, Note note) {
        if(!noteMap.containsKey(time)) {
            noteMap.put(time, new ArrayList<>());
        }
        noteMap.get(time).add(note);
    }

    public final void addCallback(NoteCallback callback) {
        callbacks.add(callback);
    }

    public void play() {
        if(isPlaying) {throw new IllegalStateException("Already playing");}
        this.isPlaying = true;

        for(NoteCallback callback : callbacks) {
            callback.onSongStarted(0);
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
                            for(Note note : msgs) {
                                callback.onNotePlayed(note, newTime);
                            }
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

    public void stop() {
        this.isPlaying = false;
    }

}
