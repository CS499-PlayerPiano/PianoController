package plu.capstone.playerpiano.sheetmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;

/**
 * Base class for any type of sheet music.
 * Can be extended, or used as is.
 */
public class SheetMusic {

    private final Logger logger = new Logger(this);

    @Getter
    private Map<Long, List<SheetMusicEvent>> eventMap = new HashMap<>();

    @Getter
    protected long songLengthMS = 0;

    @Getter
    private boolean isPlaying = false;
    private List<SheetMusicCallback> callbacks = new ArrayList<>();

    /**
     * Creates a new SheetMusic object.
     * @param time The time in milliseconds that this note starts.
     * @param note The note to add.
     */
    @Deprecated
    public final void putNote(long time, Note note) {
        putEvent(time, note);
    }

    /**
     * Creates a new SheetMusic object.
     * @param time The time in milliseconds that this event starts.
     * @param event The event to add.
     */
    public final void putEvent(long time, SheetMusicEvent event) {
        if(!eventMap.containsKey(time)) {
            eventMap.put(time, new ArrayList<>());
        }
        eventMap.get(time).add(event);
    }

    /**
     * Adds a callback to this SheetMusic object.
     * @param callback The callback to add.
     */
    public final void addCallback(SheetMusicCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Starts playing this SheetMusic object.
     */
    public void play() {
        if(isPlaying) {throw new IllegalStateException("Already playing");}
        this.isPlaying = true;

        for(SheetMusicCallback callback : callbacks) {
            callback.onTimestampEvent(0, songLengthMS);
            callback.onSongStarted(0, eventMap);
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

                    for(SheetMusicCallback callback : callbacks) {
                        final long newTime = prevTime + callback.getOffset();
                        List<SheetMusicEvent> msgs = eventMap.getOrDefault(newTime, null);

                        if(msgs != null) {
                            callback.onEventsPlayed(msgs, newTime);
                        }
                    }
                }
            }

            for(SheetMusicCallback callback : callbacks) {
                callback.onTimestampEvent(time, songLengthMS);
            }

            prevTime = time;

        }

        for(SheetMusicCallback callback : callbacks) {
            callback.onTimestampEvent(songLengthMS, songLengthMS); //While we should be at the end, we may not be.
            callback.onSongFinished(prevTime);
        }

    }

    /**
     * Stops playing this SheetMusic object.
     */
    public void stop() {
        this.isPlaying = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof SheetMusic)) return false;

        SheetMusic that = (SheetMusic) o;

        if (songLengthMS != that.songLengthMS) return false;
        if (isPlaying != that.isPlaying) return false;

        return eventMap.equals(that.eventMap);
    }


    @Override
    public int hashCode() {
        int result = eventMap.hashCode();
        result = 31 * result + (int) (songLengthMS ^ (songLengthMS >>> 32));
        result = 31 * result + (isPlaying ? 1 : 0);
        return result;
    }

}
