package plu.capstone.playerpiano.controller.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.controller.plugins.PluginArduinoPiano.PluginRealPiano;
import plu.capstone.playerpiano.controller.plugins.PluginLogger.PluginLogger;
import plu.capstone.playerpiano.controller.plugins.PluginPianoEmulatorGui.PluginGui;
import plu.capstone.playerpiano.controller.plugins.PluginSynth.PluginSynth;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.controller.plugins.test.PluginConfigTester;
import plu.capstone.playerpiano.controller.plugins.test.TestPlugin;

@AllArgsConstructor
public enum PluginInstances {

    PHYSICAL_PIANO(PluginRealPiano.class),
    LOGGER(PluginLogger .class),
    EMULATOR(PluginGui.class),
    SYNTH(PluginSynth.class),
    WEB_SERVER(PluginWebAPI.class),

    //TEST_PLUGIN(TestPlugin.class),
    //TEST_PLUGIN_CONFIG(PluginConfigTester.class)
    ;

    private final Class<? extends Plugin> pluginClass;

    public static Class<? extends Plugin>[] getAllPlugins() {
        Class<? extends Plugin>[] plugins = new Class[PluginInstances.values().length];
        for(int i = 0; i < PluginInstances.values().length; i++) {
            plugins[i] = PluginInstances.values()[i].pluginClass;
        }
        return plugins;
    }

}
