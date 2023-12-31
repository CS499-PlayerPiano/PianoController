package plu.capstone.playerpiano.controller.plugins.PluginArduinoPiano;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.utilities.MathUtilities;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

/**
 * Plugin to communicate with an Arduino to play notes on a real piano.
 *
 * Packets:
 *  Start Song: 'S'
 *  End Song: 'F'
 *  Note List: 'N'
 *      (byte) Number of notes
 *      Array of notes:
 *          (byte) Key
 *          (byte) IsOn
 *          (byte) Velocity
 */
public class PluginRealPiano extends Plugin {

    private final Logger loggerArduino = new Logger(logger, "Arduino");

    private static final int PARITY_NONE = 0;
    private static final int STOP_BITS = 1;
    private static final int DATA_BITS = 8;


    private SerialPort arduino;

    /**
     * Set the default values for the config file before it is loaded.
     */
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
        arduino.setComPortParameters(config.getInteger("baudRate"), DATA_BITS, STOP_BITS, PARITY_NONE);
        arduino.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        if(arduino.openPort()) {
            logger.info("Successfully opened port " + COM_PORT);
        }
        else {
            logger.error("Failed to open port " + COM_PORT);
        }

        //set up arduino receiving code
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
                loggerArduino.info("Received: " + new String(newData));

            }
        });
    }


    /**
     * Called when a note is played or changed. We will send a packet to the arduino to tell it to play the note(s)
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    @Override
    public void onNotesPlayed(List<Note> notes, long timestamp) {

        if(!arduino.isOpen()) {return;}

        ByteBuffer buffer = ByteBuffer.allocate(2 + (notes.size() * 3));

        /*
        N - Packet to tell the arduino we are sending a list of notes
        Number of notes
        Array of notes:
            Key
            IsOn
            Velocity
         */

        buffer.put((byte) 'N');
        buffer.put((byte) notes.size());

        for(Note note : notes) {
            buffer.put((byte) note.toPianoKey());
            buffer.put((byte) (note.isNoteOn() ? 1 : 0));

            byte velocity = 0;

            if(note.isNoteOn()){
                // Map the velocity from 0-127 to 106-235
                //TODO: Make this configurable
                velocity = (byte) MathUtilities.map(note.getVelocity(), 0, 127, 106, 255);
            }

            buffer.put(velocity);
        }

        writeBytes(buffer.array());

    }

    /**
     * Nicely formats a byte array into a string. This is used for debugging.
     * All bytes are converted to their integer value, except for newlines, which are converted to "NL"
     * @param arr byte array to convert
     * @return nicely formatted string of bytes
     */
    private String byteArrayToStringColored(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < arr.length; i++) {
            byte b = arr[i];


            if (i == 0) {
                sb.append(ConsoleColors.RED);
            }
            if(arr[0] == 'N') {
                if (i == 1) {
                    sb.append(ConsoleColors.YELLOW);
                } else if ((i - 2) % 3 == 0) {
                    sb.append(ConsoleColors.GREEN);
                } else if ((i - 2) % 3 == 1) {
                    sb.append(ConsoleColors.BLUE);
                } else if ((i - 2) % 3 == 2) {
                    sb.append(ConsoleColors.PURPLE);
                }
            }

            if(b == '\n') {
                sb.append("NL");
            } else {
                sb.append(b & 0xFF);
            }

            sb.append(ConsoleColors.BLACK_BRIGHT);

            if(i != arr.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        if(!arduino.isOpen()) {return;}

        /*
         * S - Packet to tell the arduino we are starting a song
         */

        byte[] data = {
                'S'
        };

        writeBytes(data);
    }

    @Override
    public void onSongFinished(long timestamp) {
        if(!arduino.isOpen()) {return;}

        /*
         * F - Packet to tell the arduino to turn off all notes, we have finished playing the song
         */

        byte[] data = {
                'F'
        };

        writeBytes(data);
    }

    private void writeBytes(byte[] data) {
        logger.debug("Sending " + byteArrayToStringColored(data));
        arduino.writeBytes(data, data.length);
    }
}
