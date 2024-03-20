package plu.capstone.playerpiano.programs.maincontroller;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import lombok.Getter;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PacketIds;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.plugins.Plugin;
import plu.capstone.playerpiano.plugins.PluginLoader;
import plu.capstone.playerpiano.programs.miditopianofile.MidiCleanerSM;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

public class PlayerPianoController implements Runnable {

    private static PlayerPianoController INSTANCE;
    private final Logger logger = new Logger(this);

    @Getter
    private PluginLoader pluginLoader;

    @Getter
    private QueueManager queueManager;

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
        pluginLoader.loadFromPluginEnum();

        for(Plugin plugin : pluginLoader.getPlugins()) {
            plugin.loadPlugin();
        }

        queueManager = new QueueManager(this);
        queueManager.start();





        try {

            Thread.sleep(5000);
            System.out.println("Queueing song");

            queueManager.queueSong(MidiCleanerSM.applyChanges(new MidiSheetMusic(new File("res/songs-db/songs/12th_Street_Rag.mid"))));
            //queueManager.queueSong(MidiCleanerSM.applyChanges(new MidiSheetMusic(new File("res/songs-db/songs/RUSH_E_FINAL.mid"))));
            //queueManager.queueSong(new MidiSheetMusic(new File("res/songs-db/songs/RUSH_E_FINAL.mid")));
            //queueManager.queueSong(MidiCleanerSM.applyChanges(new MidiSheetMusic(new File("res/songs-db/songs/Beethoven_Fur_Elise.mid"))));
           // queueManager.queueSong(new MidiSheetMusic(new File("res/songs-db/songs/Beethoven_Fur_Elise.mid")));
            //queueManager.queueSong(MidiCleanerSM.applyChanges(new MidiSheetMusic(new File("res/songs-db/songs/TEST_full_scale.mid"))));
            //queueManager.queueSong(new MidiSheetMusic(new File("tmp/full_scale_test.MID")));
            //queueManager.queueSong(new MidiSheetMusic(new File("tmp/Hungarian_Rhapsody_No.2_Friska_-_Franz_Liszt.mid")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //The downside to using plugins...
    public void sendWSPacket(PacketIds id) {
        sendWSPacket(id, new JsonObject());
    }
    public void sendWSPacket(PacketIds id, JsonObject data) {
        Plugin plugin = pluginLoader.findPluginByName("PluginWebAPI");
        if(plugin != null && plugin.isEnabled() && plugin instanceof PluginWebAPI) {
            PluginWebAPI webAPI = (PluginWebAPI) plugin;

            webAPI.sendWSPacket(id, data);
        }
    }

}
