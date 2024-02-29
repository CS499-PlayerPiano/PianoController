package plu.capstone.playerpiano.plugins;

import io.javalin.plugin.PluginManager;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.plugins.impl.PluginArduinoPiano.PluginRealPiano;
import plu.capstone.playerpiano.plugins.impl.PluginLogger.PluginLogger;
import plu.capstone.playerpiano.plugins.impl.PluginMidiKeyboard.PluginMidiKeyboard;
import plu.capstone.playerpiano.plugins.impl.PluginPatternDebugGui.PluginPatternDebugGui;
import plu.capstone.playerpiano.plugins.impl.PluginPianoEmulatorGui.PluginVirtualPianoEmulatorGui;
import plu.capstone.playerpiano.plugins.impl.PluginSynth.PluginSynth;
import plu.capstone.playerpiano.plugins.impl.PluginSynthesiaClone.PluginSynthesiaGui;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.programs.maincontroller.PlayerPianoController;

@AllArgsConstructor
public enum PluginInstances {

    PHYSICAL_PIANO(PluginRealPiano.class),
    LOGGER(PluginLogger .class),
    EMULATOR(PluginVirtualPianoEmulatorGui.class),
    SYNTH(PluginSynth.class),
    WEB_SERVER(PluginWebAPI.class),
    MIDI_KEYBOARD(PluginMidiKeyboard.class),
    PATTERN_DEBUG(PluginPatternDebugGui.class),
    SYNTHESIA_GUI(PluginSynthesiaGui.class),
    ;

    private final Class<? extends Plugin> pluginClass;

    public static Class<? extends Plugin>[] getAllPlugins() {
        Class<? extends Plugin>[] plugins = new Class[PluginInstances.values().length];
        for(int i = 0; i < PluginInstances.values().length; i++) {
            plugins[i] = PluginInstances.values()[i].pluginClass;
        }
        return plugins;
    }

    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }
}
