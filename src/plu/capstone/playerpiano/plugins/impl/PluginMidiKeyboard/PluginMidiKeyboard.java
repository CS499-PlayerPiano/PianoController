package plu.capstone.playerpiano.plugins.impl.PluginMidiKeyboard;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import plu.capstone.playerpiano.plugins.Plugin;

public class PluginMidiKeyboard extends Plugin {


    /**
     * Set the default values for the config file before it is loaded.
     */
    @Override
    public void setDefaultConfigValues() {
        config.setString("deviceName", "AKM320");
        config.setBoolean("ignoreVelocity", true);
        config.setBoolean("printAllDeviceNames", false);
    }

    @Override
    protected void onEnable() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {

            if(config.getBoolean("printAllDeviceNames", false)) {
                logger.debug("Found midi device: " + info.toString());
            }

            if(info.getName().equals(config.getString("deviceName", ""))) {
                try {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    try {
                        device.getTransmitter().setReceiver(new MidiInputReceiver(this));
                        device.open();
                        logger.info("Opened Midi device!");
                    }
                    catch(MidiUnavailableException e) {
                        // We ignore this error, because I can't figure out how to tell if a device is a input or output device
                    }

                } catch (MidiUnavailableException e) {
                    logger.error("Midi device unavailable!", e);
                }
            }




        }
    }
}
