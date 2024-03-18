package plu.capstone.playerpiano.sheetmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.utilities.Stopwatch;

/**
 * Base class for any type of sheet music.
 * Can be extended, or used as is.
 */
public class SheetMusic {

    private final Logger logger = new Logger(this);

    @Getter
    private Map<Long, List<SheetMusicEvent>> eventMap = new HashMap<>();

    @Getter
    @Setter
    protected long songLengthMS = 0;

    @Getter
    private boolean isSheetMusicStillScrolling = false;
    private List<SheetMusicCallback> callbacks = new ArrayList<>();

    private final Stopwatch stopwatch = new Stopwatch();

    /**
     * Put an event into the event map at the given time.
     * This can be a Note, TempoChangeEvent, or any other SheetMusicEvent.
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
     * Overwrites the event map with a new one.
     * Used for song transformations, don't use this method normally!
     * @param eventMap
     */
    public void overwriteEventMap(Map<Long, List<SheetMusicEvent>> eventMap) {
        this.eventMap = eventMap;
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
        if(isSheetMusicStillScrolling) {throw new IllegalStateException("Already playing");}
        this.isSheetMusicStillScrolling = true;

        for(SheetMusicCallback callback : callbacks) {
            callback.onTimestampEvent(0, songLengthMS);
            callback.onSongStarted(0, eventMap);
        }

        stopwatch.start();

        long prevTime = -1_000;
        while(isSheetMusicStillScrolling) {

            long time = stopwatch.getElapsedTimeMS();
            if(time > songLengthMS) {
                stop();
                return;
            }

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

            for(SheetMusicCallback callback : callbacks) {
                callback.onTimestampEvent(time, songLengthMS);
            }

            prevTime = time;

        }
    }

    public void setPaused(boolean paused) {
        if(paused) {
            stopwatch.stop();
        } else {
            stopwatch.start();
        }

        for(SheetMusicCallback callback : callbacks) {
            if(paused) {
                callback.onPause();
            } else {
                callback.onUnpause();
            }
        }
    }

    public boolean isPaused() {
        return !stopwatch.isRunning();
    }

    /**
     * Stops playing this SheetMusic object.
     */
    public void stop() {

        stopwatch.stop();

        for(SheetMusicCallback callback : callbacks) {
            callback.onTimestampEvent(songLengthMS, songLengthMS); //While we should be at the end, we may not be.
            callback.onSongFinished(songLengthMS);
        }

        this.isSheetMusicStillScrolling = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof SheetMusic)) return false;

        SheetMusic that = (SheetMusic) o;

        if (songLengthMS != that.songLengthMS) return false;

        return eventMap.equals(that.eventMap);
    }

    @Override
    public String toString() {
        return "SheetMusic{" +
                "eventMapLen=" + eventMap.size() +
                ", songLengthMS=" + songLengthMS +
                '}';

    }

    @Override
    public int hashCode() {
        int result = eventMap.hashCode();
        result = 31 * result + (int) (songLengthMS ^ (songLengthMS >>> 32));
        return result;
    }

}
