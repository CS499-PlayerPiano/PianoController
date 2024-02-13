package plu.capstone.playerpiano.plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.programs.maincontroller.PlayerPianoController;
import plu.capstone.playerpiano.QueueManager.QueuedSongWithMetadata;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;
import plu.capstone.playerpiano.sheetmusic.events.TempoChangeEvent;

/**
 * Base class for all plugins.
 */
public abstract class Plugin implements SheetMusicCallback {

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    @Getter
    private boolean enabled;

    @Getter
    private final String name = getClass().getSimpleName();

    @Getter
    protected PluginConfig config = new PluginConfig(this);

    @Getter
    protected final Logger logger = new Logger(this);

    private final Queue<TimedEvents> eventQueue = new ConcurrentLinkedQueue<>();


    /**
     * Called when the plugin is enabled.
     */
    protected void onEnable() {};

    /**
     * Called when the plugin is disabled.
     */
    protected void onDisable() {};

    /**
     * Called by the plugin manager to load the plugin.
     * You can not override this method. Use {@link #onEnable()} instead.
     */
    public final void loadPlugin() {
        config.loadConfig();
        if(enabled) return;
        if(!config.getBoolean("enabled", true)) return;

        enabled = true;

        new Thread(() -> {

            logger.info("Hello world!");
            onEnable();

            while(enabled) {
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

            logger.info("Goodbye!");
            onDisable();

            eventQueue.clear();

        }, this.name + " - Master Thread").start();
    }

    /**
     * Called by the plugin manager to disable the plugin.
     * You can not override this method. Use {@link #onDisable()} instead.
     */
    public final void disablePlugin() {
        if(!enabled) return;
        enabled = false;
    }

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
            else if(event instanceof TempoChangeEvent) {
                onTempoChangeEvent((TempoChangeEvent) event, timestamp);
            }
            else if(event instanceof SustainPedalEvent) {
                onSustainPedal((SustainPedalEvent) event, timestamp);
            }
        }

        onNotesPlayed(notes, timestamp);
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
     * Called when a time change event is played / state changed at a given timestamp.
     * Only supported with version 2 file formats!
     * @param event the event that was played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     */
    public void onTempoChangeEvent(TempoChangeEvent event, long timestamp) {}

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

    /////////// Shortcuts for PlayerPianoController methods ///////////
    //Generate ascii art text as a comment saying: "Shortcuts"

    /**
     * Plays a single note.
     * @param note The note to play live.
     */
    public final void playNote(Note note) {
        this.playNotes(List.of(note));
    }

    /**
     * Plays multiple notes.
     * @param notes The notes to play live.
     */
    public final void playNotes(List<Note> notes) {
        PlayerPianoController.getInstance().getQueueManager().playNotes(notes);
    }

    /**
     * Play a given sheet music.
     * @param music The sheet music to play.
     * @return the position in the queue.
     */
    @Deprecated
    public final int playSheetMusic(SheetMusic music) {
        return PlayerPianoController.getInstance().getQueueManager().queueSong(music);
    }

    /**
     * Play a given sheet music.
     * @param music The sheet music to play.
     * @return the position in the queue.
     */
    public final int playSheetMusic(QueuedSongWithMetadata music) {
        return PlayerPianoController.getInstance().getQueueManager().queueSong(music);
    }

    /**
     * Stops the current sheet music.
     */
    public final void stopSheetMusic() {
        PlayerPianoController.getInstance().getQueueManager().stopSheetMusic();
    }

    /**
     * Returns true if the sheet music is currently playing.
     * @return true if the sheet music is currently playing.
     */
    public final boolean isSheetMusicPlaying() {
        return PlayerPianoController.getInstance().getQueueManager().isSheetMusicPlaying();
    }

    public final void skipSong() {
        PlayerPianoController.getInstance().getQueueManager().skipSong();
    }

    /**
     * This function is when the plugin is first loaded and the config file is created for the first time.
     * This is called BEFORE {@link #onEnable()}.
     */
    public void setDefaultConfigValues() {}


    @AllArgsConstructor
    @Getter
    public class TimedEvents {
        private final long timestamp;
        private final List<SheetMusicEvent> events;
    }
}
