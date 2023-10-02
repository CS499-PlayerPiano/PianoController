package plugins;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import plu.capstone.playerpiano.controller.logger.Logger;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.utilities.MathUtilities;

public class PluginRealPiano extends Plugin {

    private final Logger logger = new Logger(this);
    private final Logger loggerArduino = new Logger(logger.getName() + "-Arduino");

    private static final int PARITY_NONE = 0;
    private static final int STOPBITS = 1;
    private static final int DATABITS = 8;

    //turn into config options
//    private static final int BAUD_RATE = 19200;
//    private static final String COM_PORT = "COM3";

    private SerialPort arduino;

    @Override
    public void setDefaultConfigValues() {

        config.setString("comPort", "COM10");
        config.setInteger("baudRate", 19200);
        config.setBoolean("printDebugOutputFromArduino", true);

    }

    @Override
    protected void onEnable() {

        final String COM_PORT = config.getString("comPort");

        arduino = SerialPort.getCommPort(COM_PORT);
        arduino.setComPortParameters(config.getInteger("baudRate"), DATABITS, STOPBITS, PARITY_NONE);
        arduino.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        if(arduino.openPort()) {
            logger.info("Successfully opened port " + COM_PORT);
        } else {
            logger.error("Failed to open port " + COM_PORT);
        }

        arduino.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {return new byte[] { (byte)'\n' };}

            @Override
            public boolean delimiterIndicatesEndOfMessage() {return true;}

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {

                if(event.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                    return;
                }

                byte[] newData = event.getReceivedData();
                String str = new String(newData);
                logger.info(str);

            }
        });
    }

    @Override
    public void onNotePlayed(Note note, long timestamp) {

        if(!arduino.isOpen()) {return;}

        final int key = note.getKeyNumber();
        final int isOn = note.isNoteOn() ? 1 : 0;
        final int velocity = MathUtilities.map(note.getVelocity(), 0, 127, 106, 235);

        byte[] data = {
                'N',
                (byte) key,
                (byte) isOn,
                (byte) velocity,
        };

        arduino.writeBytes(data, data.length);

    }

    @Override
    public void onSongFinished(long timestamp) {
        if(!arduino.isOpen()) {return;}

        byte[] data = {
                'O'
        };
        arduino.writeBytes(data, data.length);
    }
}
