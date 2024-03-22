package plu.capstone.playerpiano.plugins.impl.PluginArduinoPiano;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.plugins.PluginConfig;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.plugins.Plugin;
import plu.capstone.playerpiano.utilities.MathUtilities;
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

    //NOTE: This is backwards then what is stored in the config file!
    //Key: Midi Note
    //Value: Key physically
    private Map<Integer, Integer> noteMapping = new HashMap<>();

    private int velocityMappingMin = 106;
    private int velocityMappingMax = 255;
    private boolean ignoreVelocity = false;

    /**
     * Set the default values for the config file before it is loaded.
     */
    @Override
    public void setDefaultConfigValues() {
        config.setString("comPort", "COM3");
        config.setInteger("baudRate", 115200);
        config.setBoolean("printDebugOutputFromArduino", true);
        config.setBoolean("ignoreVelocity", false);

        PluginConfig velocityMappingConfig = new PluginConfig(this);
        velocityMappingConfig.setInteger("min", velocityMappingMin);
        velocityMappingConfig.setInteger("max", velocityMappingMax);
        config.setNestedConfig("velocityMapping", velocityMappingConfig);

        PluginConfig noteMappingConfig = new PluginConfig(this);

        //Note index from 0-88 to midi note
        for(int i = 0; i < 88; i++) {
            int midiNote = i + 21;
            noteMappingConfig.setInteger(Integer.toString(i), midiNote);
        }

        config.setNestedConfig("noteMapping", noteMappingConfig);


    }

    @Override
    protected void onEnable() {

        logger.setDebugEnabled(true);

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

        //NOTE: This is backwards then what is stored in the config file!
        //Config: Key -> Midi Note
        //This: Midi Note -> Key
        PluginConfig noteMappingConfig = config.getNestedConfig("noteMapping");
        for(int keyIndex = 0; keyIndex < 88; keyIndex++) {
            int midiNote = noteMappingConfig.getInteger(Integer.toString(keyIndex));
            noteMapping.put(midiNote, keyIndex);
        }

        PluginConfig velocityMappingConfig = config.getNestedConfig("velocityMapping");
        velocityMappingMin = velocityMappingConfig.getInteger("min");
        velocityMappingMax = velocityMappingConfig.getInteger("max");

        ignoreVelocity = config.getBoolean("ignoreVelocity");
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

        //We have two different packets we can send to the arduino, a note packet and a batch note packet.
        //Depending on the size of the packet, we will send the smaller one.
        //It just depends how many notes and the type are hit.
        final byte[] nPacket = noteArrayToNPacket(notes);
        final byte[] bPacket = noteArrayToBPacket(notes);

        System.out.println("nPacket: " + nPacket.length);
        System.out.println("bPacket: " + bPacket.length);
        System.out.println();

        //Write the smaller packet to the arduino.
        //If they are equal, we use the N packet

        byte[] dataToBeWritten = nPacket;
        if(bPacket.length < nPacket.length) {
            dataToBeWritten = bPacket;
            System.out.println("Using B Packet");
        }


        writeBytes(dataToBeWritten);

    }

    /*
    Batch note packet format:
        B - Packet to tell the arduino we are sending a batch change of notes
        Number of batches
        Array of notes:
            Key
            IsOn
            Velocity
    */
    private byte[] noteArrayToBPacket(List<Note> notes) {

        //sort the notes by key number
        notes.sort(Comparator.comparingInt(Note::getKeyNumber));

        //batch the notes into list of notes that are the same on and velocity
        List<List<Note>> batchedNotes = new ArrayList<>();
        for(Note note : notes) {
            boolean found = false;
            for(List<Note> batch : batchedNotes) {
                if(batch.get(0).isNoteOn() == note.isNoteOn() && batch.get(0).getVelocity() == note.getVelocity()) {
                    batch.add(note);
                    found = true;
                    break;
                }
            }
            if(!found) {
                List<Note> newBatch = new ArrayList<>();
                newBatch.add(note);
                batchedNotes.add(newBatch);
            }
        }

        //B continousNum, starting, velcoity
        ByteBuffer buffer = ByteBuffer.allocate(2 + (batchedNotes.size() * 3));
        buffer.put((byte) 'B');
        buffer.put((byte) batchedNotes.size());

        for(List<Note> batch : batchedNotes) {
            byte startingKey = (byte) batch.get(0).getKeyNumber();
            byte contiguous = (byte) (batch.size() - startingKey + 1); //Key index is 0 based
            byte velocity = (byte) batch.get(0).getVelocity();

            buffer.put(contiguous);
            buffer.put(startingKey);
            buffer.put(velocity);
        }

        return buffer.array();

    }

    /*
    Note packet format:
        N - Packet to tell the arduino we are sending a list of notes
        Number of notes
        Array of notes:
            Key
            Velocity
    */
    private byte[] noteArrayToNPacket(List<Note> notes) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + (notes.size() * 2));



        buffer.put((byte) 'N');
        buffer.put((byte) notes.size());

        for(Note note : notes) {
            if(!note.isValidPianoKey()) {continue;} //Ignore invalid notes
            Integer keyIndex = noteMapping.get(note.getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + note.toPianoKey());
                continue;
            }
            buffer.put((byte) (int)keyIndex);

            //If the note is on, the velocity isn't 0. If the note is off, the velocity is 0
            byte velocity = 0;

            if(note.isNoteOn()){
                // Map the velocity from 0-127 to 106-235
                if(ignoreVelocity) {
                    velocity = (byte) velocityMappingMax;
                }
                else {
                    velocity = (byte) MathUtilities.map(note.getVelocity(), 0, 127, velocityMappingMin, velocityMappingMax);
                }
            }
            else {
                velocity = 0;
            }

            buffer.put(velocity);
        }

        return buffer.array();
    }

    //TODO: Remove this method in refactor!
    @Deprecated
    public void sendRawIndexWithoutMapping(int index, boolean isOn, byte velocity) {
        if(!arduino.isOpen()) {return;}

        ByteBuffer buffer = ByteBuffer.allocate(5);

        if(!isOn) {
            velocity = 0;
        }

        buffer.put((byte) 'N');
        buffer.put((byte) 1);
        buffer.put((byte) index);
        buffer.put(velocity);

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

            sb.append(b & 0xFF);

            sb.append(ConsoleColors.BLACK_BRIGHT);

            if(i != arr.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void onPause() {
        if(!arduino.isOpen()) {return;}

        /*
         * P - Packet to tell the arduino to turn off all notes, we have paused the song
         */

        byte[] data = {
                'P'
        };

        writeBytes(data);
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
        //logger.debug("Sending " + byteArrayToStringColored(data));
        arduino.writeBytes(data, data.length);
    }
}
