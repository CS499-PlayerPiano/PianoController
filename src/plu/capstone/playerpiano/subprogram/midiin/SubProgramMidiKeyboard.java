package plu.capstone.playerpiano.subprogram.midiin;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.subprogram.SubProgram;

public class SubProgramMidiKeyboard extends SubProgram {

    //TODO: Config!
    private String DEVICE_NAME = "AKM320";
    protected boolean IGNORE_VELOCITY = true;
    private boolean PRINT_ALL_DEVICE_NAMES = true;

    private final Logger logger = new Logger(this);
    @Override
    public String getSubCommand() {
        return "midi-keyboard";
    }

    @Override
    public void run() throws Exception {

        DEVICE_NAME = getSubProgramConfig().getString("deviceName", DEVICE_NAME);
        IGNORE_VELOCITY = getSubProgramConfig().getBoolean("ignoreVelocity", IGNORE_VELOCITY);
        PRINT_ALL_DEVICE_NAMES = getSubProgramConfig().getBoolean("printAllDeviceNames", PRINT_ALL_DEVICE_NAMES);


        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {

            if(PRINT_ALL_DEVICE_NAMES) {
                logger.debug("Found midi device: " + info.toString());
            }

            if(info.getName().equals(DEVICE_NAME)) {
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
