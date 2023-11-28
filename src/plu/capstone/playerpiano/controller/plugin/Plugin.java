package plu.capstone.playerpiano.controller.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import lombok.Getter;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.midi.NoteCallback;
import plu.capstone.playerpiano.controller.midi.SheetMusic;
import plu.capstone.playerpiano.logger.Logger;

/**
 * Base class for all plugins.
 */
public abstract class Plugin implements NoteCallback {

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    @Getter
    private boolean enabled;

    @Getter
    private final String name = getClass().getSimpleName();
    private final File CONFIG_FILE = new File("plugins/config/" + name + ".json");

    @Getter
    protected PluginConfig config = new PluginConfig(this);

    @Getter
    protected final Logger logger = new Logger(this);


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
        logger.info("Hello world!");
        onEnable();
    }

    /**
     * Called by the plugin manager to disable the plugin.
     * You can not override this method. Use {@link #onDisable()} instead.
     */
    public final void disablePlugin() {
        if(!enabled) return;
        enabled = false;
        logger.info("Goodbye!");
        onDisable();
    }

    /**
     * Called when X note(s) are played / state changed at a given timestamp.
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    @Override
    public void onNotesPlayed(Note[] notes, long timestamp) {
        for(Note note : notes) {
            onNotePlayed(note, timestamp);
        }
    }

    /**
     * Called when a single note is played / state changed at a given timestamp.
     * This method is not called if you override {@link #onNotesPlayed(Note[], long)}.
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
        this.playNotes(new Note[] { note });
    }

    /**
     * Plays multiple notes.
     * @param notes The notes to play live.
     */
    public final void playNotes(Note[] notes) {
        PlayerPianoController.getInstance().playNotes(notes);
    }

    /**
     * Play a given sheet music.
     * @param music The sheet music to play.
     */
    public final void playSheetMusic(SheetMusic music) {
        PlayerPianoController.getInstance().playSheetMusic(music);
    }

    /**
     * Stops the current sheet music.
     */
    public final void stopSheetMusic() {
        PlayerPianoController.getInstance().stopSheetMusic();
    }

    /**
     * Returns true if the sheet music is currently playing.
     * @return true if the sheet music is currently playing.
     */
    public final boolean isSheetMusicPlaying() {
        return PlayerPianoController.getInstance().isSheetMusicPlaying();
    }

    /**
     * This function is when the plugin is first loaded and the config file is created for the first time.
     * This is called BEFORE {@link #onEnable()}.
     */
    public void setDefaultConfigValues() {}
}
