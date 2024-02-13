package plu.capstone.playerpiano.controller.plugin;

import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.controller.plugins.PluginArduinoPiano.PluginRealPiano;
import plu.capstone.playerpiano.controller.plugins.PluginLogger.PluginLogger;
import plu.capstone.playerpiano.controller.plugins.PluginMidiKeyboard.PluginMidiKeyboard;
import plu.capstone.playerpiano.controller.plugins.PluginPatternDebugGui.PluginPatternDebugGui;
import plu.capstone.playerpiano.controller.plugins.PluginPianoEmulatorGui.PluginVirtualPianoEmulatorGui;
import plu.capstone.playerpiano.controller.plugins.PluginSynth.PluginSynth;
import plu.capstone.playerpiano.controller.plugins.PluginSynthesiaClone.PluginSynthesiaGui;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;

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

}
