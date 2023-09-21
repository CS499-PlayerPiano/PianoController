package plu.capstone.playerpiano.controller.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import lombok.Getter;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.logger.Logger;
import plu.capstone.playerpiano.controller.midi.NoteCallback;
import plu.capstone.playerpiano.controller.midi.SheetMusic;

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



    protected void onEnable() {};
    protected void onDisable() {};

    public void loadPlugin() {
        config.loadConfig();
        if(enabled) return;
        if(!config.getBoolean("enabled", true)) return;

        enabled = true;
        logger.info("Hello world!");
        onEnable();
    }

    public void setDisabled() {
        if(!enabled) return;
        enabled = false;
        logger.info("Goodbye!");
        onDisable();
    }

    @Override
    public void onNotePlayed(Note note, long timestamp) {};

    /////////// Shortcuts for PlayerPianoController methods ///////////
    //Generate ascii art text as a comment saying: "Shortcuts"

    public final void playNote(Note note) {
        PlayerPianoController.getInstance().playNote(note);
    }

    public final void playSheetMusic(SheetMusic music) {
        PlayerPianoController.getInstance().playSheetMusic(music);
    }

    public final void stopSheetMusic() {
        PlayerPianoController.getInstance().stopSheetMusic();
    }

    public final boolean isSheetMusicPlaying() {
        return PlayerPianoController.getInstance().isSheetMusicPlaying();
    }

    public void setDefaultConfigValues() {}
}
