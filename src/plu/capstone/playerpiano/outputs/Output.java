package plu.capstone.playerpiano.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AccessLevel;
import lombok.Getter;
import plu.capstone.playerpiano.JsonConfigWrapper;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

public abstract class Output implements SheetMusicCallback {

    public abstract String getName();

    @Getter
    protected final Logger logger = new Logger("Output-" + getName());

//    @Getter
//    private final String name = getClass().getSimpleName();



    private final Queue<TimedEvents> eventQueue = new ConcurrentLinkedQueue<>();

    @Getter(AccessLevel.PROTECTED) private JsonConfigWrapper config;

    /**
     * Called when the output is enabled
     */
    protected void onEnable() {};

    //Do we need to implement this / remove this?
    @Deprecated
    protected void setDefaultConfigValues() {}

    /**
     * Only call this once on the first load!
     */
    public void load(JsonConfigWrapper config) {
        this.config = config;

        new Thread(() -> {

            logger.info("Output Loaded!");
            onEnable();

            while(true) {
                TimedEvents timedNotes = eventQueue.poll();
                if(timedNotes == null) {
                    try {
                        //so we don't spin the cpu
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {}
                    continue;
                }

                splitEvents(timedNotes.getEvents(), timedNotes.getTimestamp());
            }

        }, "Output " + this.getName() + " - Master Thread").start();
    }

    /**
     * Called when X events(s) are played / state changed at a given timestamp.
     * @param events array of event that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    @Override
    public final void onEventsPlayed(List<SheetMusicEvent> events, long timestamp) {
        eventQueue.add(new TimedEvents(timestamp, events));
    }

    /**
     * Called when a sustain pedal event is played / state changed at a given timestamp.
     * Only supported with version 3 file formats!
     * @param event the event that was played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     */
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {}

    /**
     * Called when X note(s) are played / state changed at a given timestamp.
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    public void onNotesPlayed(List<Note> notes, long timestamp) {
        for(Note note : notes) {
            onNotePlayed(note, timestamp);
        }
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        eventQueue.clear();
    }

    @Override
    public void onSongFinished(long timestamp) {
        eventQueue.clear();
    }

    /**
     * Called when a single note is played / state changed at a given timestamp.
     * This method is not called if you override {@link #onEventsPlayed(java.util.List, long)} .
     * @param note
     * @param timestamp
     */
    public void onNotePlayed(Note note, long timestamp) {}

    /**
     * Splits the events into notes and time change events.
     * @param events events to split
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     */
    private void splitEvents(List<SheetMusicEvent> events, long timestamp) {

        List<Note> notes = new ArrayList<>();

        for(SheetMusicEvent event : events) {
            if(event instanceof Note) {
                notes.add((Note) event);
            }
            else if(event instanceof SustainPedalEvent) {
                onSustainPedal((SustainPedalEvent) event, timestamp);
            }
        }

        onNotesPlayed(notes, timestamp);
    }
}
