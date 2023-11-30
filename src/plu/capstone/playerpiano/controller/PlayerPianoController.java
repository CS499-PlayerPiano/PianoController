package plu.capstone.playerpiano.controller;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.sheetmusic.NoteCallback;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.plugin.PluginLoader;

public class PlayerPianoController implements Runnable {

    private static PlayerPianoController INSTANCE;
    private final Logger logger = new Logger(this);
    private SheetMusic currentSheetMusic;

    private PluginLoader pluginLoader;

    /**
     * Returns the single instance of the PlayerPianoController.
     * @return the single instance of the PlayerPianoController.
     */
    public static PlayerPianoController getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerPianoController();
        }
        return INSTANCE;
    }

    private PlayerPianoController() {}

    @Override
    public void run() {
        logger.info("PlayerPianoController running!");
        pluginLoader = new PluginLoader();
        pluginLoader.loadFromPackage("plugins");

        for(Plugin plugin : pluginLoader.getPlugins()) {
            plugin.loadPlugin();
        }

        try {
            playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/night of nights.mid")));
            //playSheetMusic(new MidiSheetMusic(new File("res/songs/ABBA/Money Money.mid")));
            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/mididownload.mid")));
            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/Beethoven_Virus.mid")));
            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/RUSH_E_FINAL.mid")));
            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/River_Flows_In_You.mid")));
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Plays multiple notes.
     * @param notes The notes to play live.
     */
    public void playNotes(Note[] notes) {
        this.playNotes(notes, NoteCallback.LIVE_TIMESTAMP);
    }

    /**
     * Plays multiple notes.
     * @param notes The notes to play.
     * @param timestamp The timestamp of the notes.
     *                  Use {@link NoteCallback#LIVE_TIMESTAMP} for live playing, or {@link #playNotes(Note[])} for a shortcut
     */
    public void playNotes(Note[] notes, long timestamp) {
        if(notes == null || notes.length == 0) {
            logger.warning("Attempted to play null note!");
            return;
        }
        for(Plugin plugin : pluginLoader.getPlugins()) {
            if(plugin.isEnabled()) {
                plugin.onNotesPlayed(notes, timestamp);
            }
        }
    }

    /**
     * Plays a given sheet music.
     * @param music The sheet music to play.
     */
    public void playSheetMusic(SheetMusic music) {

        //Stop the current sheet music if we are playing one
        this.stopSheetMusic();

        this.currentSheetMusic = music;

        for(Plugin plugin : pluginLoader.getPlugins()) {
            if(plugin.isEnabled()) {
                currentSheetMusic.addCallback(plugin);
            }
        }

        // We use a new thread for this, so we don't hang the main thread
        // TODO: This should be properly handled by the plugin system & locking!!
        // TODO: It seems to work, but this change will most likely break things!
        new Thread(() -> {
            currentSheetMusic.play();
        }, "Sheet music playing thread").start();

        //currentSheetMusic.play();

    }

    /**
     * Stops the current sheet music.
     */
    public void stopSheetMusic() {
        if(currentSheetMusic != null) {
            currentSheetMusic.stop();
        }
    }

    /**
     * Returns true if the sheet music is currently playing.
     * @return true if the sheet music is currently playing.
     */
    public boolean isSheetMusicPlaying() {
        if(currentSheetMusic != null) {
            return currentSheetMusic.isPlaying();
        }
        return false;
    }
}
