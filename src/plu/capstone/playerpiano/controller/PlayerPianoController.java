package plu.capstone.playerpiano.controller;

import com.google.gson.JsonObject;
import lombok.Getter;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PacketIds;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.plugins.plugin.Plugin;
import plu.capstone.playerpiano.plugins.plugin.PluginLoader;

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





//        try {
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/night of nights.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/ABBA/Money Money.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/mididownload.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/Beethoven_Virus.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/RUSH_E_FINAL.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs/Testing/River_Flows_In_You.mid")));
//            playSheetMusic(new MidiSheetMusic(new File("res/songs-db/songs/Coconut_Mall.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs-db/songs/Rockefeller_Street.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs-db/songs/Levan_Polkka.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs-db/songs/Bad_Piggies_Theme.mid")));
//            //playSheetMusic(new MidiSheetMusic(new File("res/songs-db/songs/Dance_Monkey.mid")));
//        } catch (InvalidMidiDataException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
