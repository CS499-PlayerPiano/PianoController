package plu.capstone.playerpiano.controller;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.controller.logger.Logger;
import plu.capstone.playerpiano.controller.midi.MidiSheetMusic;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.midi.NoteCallback;
import plu.capstone.playerpiano.controller.midi.SheetMusic;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.plugin.PluginLoader;

public class PlayerPianoController implements Runnable {

    private static PlayerPianoController INSTANCE;
    private final Logger logger = new Logger(this);
    private SheetMusic currentSheetMusic;

    private PluginLoader pluginLoader;

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

    public void playNote(Note note) {
        this.playNote(note, NoteCallback.LIVE_TIMESTAMP);
    }

    public void playNote(Note note, long timestamp) {
        if(note == null) {
            logger.warning("Attempted to play null note!");
            return;
        }
        for(Plugin plugin : pluginLoader.getPlugins()) {
            if(plugin.isEnabled()) {
                plugin.onNotePlayed(note, timestamp);
                plugin.onNotesPlayed(new Note[] { note }, timestamp);
            }
        }
    }

    public void playSheetMusic(SheetMusic music) {

        this.currentSheetMusic = music;

        for(Plugin plugin : pluginLoader.getPlugins()) {
            if(plugin.isEnabled()) {
                currentSheetMusic.addCallback(plugin);
            }
        }

        currentSheetMusic.play();

    }

    public void stopSheetMusic() {
        if(currentSheetMusic != null) {
            currentSheetMusic.stop();
        }
    }

    public boolean isSheetMusicPlaying() {
        if(currentSheetMusic != null) {
            return currentSheetMusic.isPlaying();
        }
        return false;
    }
}
