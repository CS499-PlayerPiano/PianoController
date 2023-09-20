package plu.capstone.playerpiano.controller.plugin;

import lombok.Getter;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.logger.Logger;
import plu.capstone.playerpiano.controller.midi.NoteCallback;
import plu.capstone.playerpiano.controller.midi.SheetMusic;

public abstract class Plugin implements NoteCallback {

    @Getter
    private boolean enabled;

    @Getter
    private final String name = getClass().getSimpleName();
    protected final Logger logger = new Logger(this);

    protected void onEnable() {};
    protected void onDisable() {};

    public void setEnabled() {
        if(enabled) return;
        enabled = true;
        logger.info("Enabling plugin: " + name);
        onEnable();
    }

    public void setDisabled() {
        if(!enabled) return;
        enabled = false;
        logger.info("Disabling plugin: " + name);
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

}
