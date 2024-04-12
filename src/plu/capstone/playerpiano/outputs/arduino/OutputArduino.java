package plu.capstone.playerpiano.outputs.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.JsonConfigWrapper;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;
import plu.capstone.playerpiano.utilities.MathUtilities;

/**
 * Plugin to communicate with an Arduino to play notes on a real piano.
 *
*/
public class OutputArduino extends Output {

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

    private static final byte[] TURN_OFF_ALL_NOTES = {
            'O'
    };

    /**
     * Set the default values for the config file before it is loaded.
     */
    @Override
    public void setDefaultConfigValues() {
//        config.setString("comPort", "COM3");
//        config.setInteger("baudRate", 115200);
//        config.setBoolean("printDebugOutputFromArduino", true);
//        config.setBoolean("ignoreVelocity", false);
//
//        PluginConfig velocityMappingConfig = new PluginConfig(this);
//        velocityMappingConfig.setInteger("min", velocityMappingMin);
//        velocityMappingConfig.setInteger("max", velocityMappingMax);
//        config.setNestedConfig("velocityMapping", velocityMappingConfig);
//
//        PluginConfig noteMappingConfig = new PluginConfig(this);
//
//        //Note index from 0-88 to midi note
//        for(int i = 0; i < 88; i++) {
//            int midiNote = i + 21;
//            noteMappingConfig.setInteger(Integer.toString(i), midiNote);
//        }
//
//        config.setNestedConfig("noteMapping", noteMappingConfig);
    }

    @Override
    public String getName() {return "Arduino";}

    @Override
    protected void onEnable() {

        logger.setDebugEnabled(true);

        final String COM_PORT = getConfig().getString("comPort");

        arduino = SerialPort.getCommPort(COM_PORT);
        arduino.setComPortParameters(getConfig().getInteger("baudRate"), DATA_BITS, STOP_BITS, PARITY_NONE);
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
                final String message = new String(newData);
                final boolean isError = message.toLowerCase().contains("error");
                final boolean isWarning = message.toLowerCase().contains("warning");
                if(isError) {
                    loggerArduino.error("Received: " + message);
                }
                else if(isWarning) {
                    loggerArduino.warning("Received: " + message);
                }
                else {
                    loggerArduino.info("Received: " + message);
                }

            }
        });

        //NOTE: This is backwards then what is stored in the config file!
        //Config: Key -> Midi Note
        //This: Midi Note -> Key
        JsonConfigWrapper noteMappingConfig = getConfig().getNestedConfig("noteMapping");
        for(int keyIndex = 0; keyIndex < 88; keyIndex++) {
            int midiNote = noteMappingConfig.getInteger(Integer.toString(keyIndex));
            noteMapping.put(midiNote, keyIndex);
        }

        JsonConfigWrapper velocityMappingConfig = getConfig().getNestedConfig("velocityMapping");
        velocityMappingMin = velocityMappingConfig.getInteger("min");
        velocityMappingMax = velocityMappingConfig.getInteger("max");

        ignoreVelocity = getConfig().getBoolean("ignoreVelocity");
    }


    /**
     * Called when a note is played or changed. We will send a packet to the arduino to tell it to play the note(s)
     * @param notes array of notes that were played / changed
     * @param timestamp timestamp of the event in milliseconds. If this is a live event, this will be {@link #LIVE_TIMESTAMP}
     *                  and the timestamp will be the time since the song started.
     */
    @Override
    public void onNotesPlayed(List<NoteEvent> notes, long timestamp) {

        if(!arduino.isOpen()) {return;}

        //We don't want to send a packet if there are no notes to play.
        if(notes.size() == 0) {
            return;
        }

        //We have two different packets we can send to the arduino, a note packet and a batch note packet.
        //Depending on the size of the packet, we will send the smaller one.
        //It just depends how many notes and the type are hit.
        String whichPacket = "N";
        final byte[] nPacket = noteArrayToNPacket(notes);
        final byte[] bPacket = noteArrayToBPacket(notes);
        final byte[] mPacket = noteArrayToMPacket(notes);

        //Write the smaller packet to the arduino.
        //If they are equal, we use the N packet

        byte[] dataToBeWritten = nPacket;
        if(bPacket.length < nPacket.length) {
            dataToBeWritten = bPacket;
            whichPacket = "B";
        }

        if(mPacket != null && mPacket.length < dataToBeWritten.length) {
            dataToBeWritten = mPacket;
            whichPacket = "M";
        }

//        logger.debug("Sending " + whichPacket + " packet: len=" + dataToBeWritten.length);

        writeBytes(dataToBeWritten);

    }

    /*
    Batch note packet format:
        B - Packet to tell the arduino we are sending a batch change of notes
        Number of batches
        Array of batches:
            contiguous
            startingKey
            Velocity
    */
    private byte[] noteArrayToBPacket(List<NoteEvent> notes) {

        //sort the notes by key number
        notes.sort(Comparator.comparingInt(NoteEvent::getKeyNumber));

        List<List<NoteEvent>> batchedNotes = new ArrayList<>();
        for(NoteEvent note : notes) {
            boolean found = false;
            for(List<NoteEvent> batch : batchedNotes) {

                Integer currentKeyIndex = noteMapping.get(note.getKeyNumber());
                if(currentKeyIndex == null) {
                    logger.error("Failed to find key index for note " + note.getKeyNumber());
                    continue;
                }

                Integer lastBatchIndex = noteMapping.get(batch.get(batch.size() - 1).getKeyNumber());
                if(lastBatchIndex == null) {
                    logger.error("Failed to find key index for note " + batch.get(batch.size() - 1).getKeyNumber());
                    continue;
                }

                if(batch.get(0).isNoteOn() == note.isNoteOn() && batch.get(0).getVelocity() == note.getVelocity()
                && currentKeyIndex == lastBatchIndex+1) {
                    batch.add(note);
                    found = true;
                    break;
                }
            }
            if(!found) {
                List<NoteEvent> newBatch = new ArrayList<>();
                newBatch.add(note);
                batchedNotes.add(newBatch);
            }
        }

        //B continousNum, starting, velcoity
        ByteBuffer buffer = ByteBuffer.allocate(2 + (batchedNotes.size() * 3));
        buffer.put((byte) 'B');
        buffer.put((byte) batchedNotes.size());

        for(List<NoteEvent> batch : batchedNotes) {
            byte contiguous = (byte) batch.size(); //Key index is 0 based

            Integer keyIndex = noteMapping.get(batch.get(0).getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + batch.get(0).getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }

            byte startingKey = (byte) (int)keyIndex;
            byte velocity = (byte) batch.get(0).getVelocity();

            if(batch.get(0).isNoteOn()) {
                velocity = mapVelocity(velocity);
            }
            else {
                velocity = 0;
            }

            buffer.put(contiguous);
            buffer.put(startingKey);
            buffer.put(velocity);
        }

        return buffer.array();

    }

    /*
    Mutiple note packet format:
        M - Packet to tell the arduino we are sending mutiple notes with the same velocity
        Number of notes
        Velocity
        Array of notes:
            Key

      WILL RETURN NULL IF VELOCITIES ARE NOT THE SAME
    */
    private byte[] noteArrayToMPacket(List<NoteEvent> notes) {

        //sort the notes by key number
        notes.sort(Comparator.comparingInt(NoteEvent::getKeyNumber));

        final NoteEvent firstNote = notes.get(0);

        int velocity = firstNote.getVelocity();
        for(NoteEvent note : notes) {
            if(note.getVelocity() != velocity) {
                return null;
            }
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(3 + notes.size());
        buffer.put((byte) 'M');
        buffer.put((byte) notes.size());

        if(firstNote.isNoteOn()) {
            velocity = mapVelocity(velocity);
        }
        else {
            velocity = 0;
        }

        buffer.put((byte) velocity);

        for(NoteEvent note : notes) {
            Integer keyIndex = noteMapping.get(note.getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + note.getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }
            buffer.put((byte) (int)keyIndex);
        }

        return buffer.array();

    }

    private final byte mapVelocity(int velocity) {
        if(ignoreVelocity) {
            return (byte) velocityMappingMax;
        }
        return (byte) MathUtilities.map(velocity, 0, 127, velocityMappingMin, velocityMappingMax);
    }

    /*
    Note packet format:
        N - Packet to tell the arduino we are sending a list of notes
        Number of notes
        Array of notes:
            Key
            Velocity
    */
    private byte[] noteArrayToNPacket(List<NoteEvent> notes) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + (notes.size() * 2));



        buffer.put((byte) 'N');
        buffer.put((byte) notes.size());

        for(NoteEvent note : notes) {
            Integer keyIndex = noteMapping.get(note.getKeyNumber());
            if(keyIndex == null) {
                logger.error("Failed to find key index for note " + note.getKeyNumber());
                keyIndex = 0; // Default to 0 i guess, that way we are never reading out of bounds
            }
            buffer.put((byte) (int)keyIndex);

            //If the note is on, the velocity isn't 0. If the note is off, the velocity is 0
            byte velocity = 0;

            if(note.isNoteOn()){
                velocity = (byte) mapVelocity(note.getVelocity());
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
        if(arduino == null || !arduino.isOpen()) {return;}

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


            String clr = null;
            if (i == 0) {
                clr = ConsoleColors.RED;
            }

            if(arr[0] == 'N' && clr == null) {
                if (i == 1) {
                    clr = ConsoleColors.YELLOW;
                } else if ((i - 2) % 2 == 0) {
                    clr = ConsoleColors.GREEN;
                } else if ((i - 2) % 2 == 1) {
                    clr = ConsoleColors.BLUE;
                }

            }
            else if(arr[0] == 'M' && clr == null) {
                if(i == 1) {
                    clr = ConsoleColors.YELLOW;
                } else if (i == 2) {
                    clr = ConsoleColors.BLUE;
                } else {
                    clr = ConsoleColors.GREEN;
                }
            }
            else if(arr[0] == 'B' && clr == null) {
                if(i == 1) {
                    clr = ConsoleColors.YELLOW;
                }
                else if((i - 2) % 4 == 0) {
                    clr = ConsoleColors.GREEN;
                }
                else if((i - 2) % 4 == 1) {
                    clr = ConsoleColors.GREEN;
                }
                else if((i - 2) % 4 == 2) {
                    clr = ConsoleColors.BLUE;
                }
                else if((i - 2) % 4 == 2) {
                    clr = ConsoleColors.PURPLE;
                }
            }
            else if(arr[0] == 'S' && clr == null) {
                clr = ConsoleColors.PURPLE;
            }

            if(clr != null) {
                sb.append(clr);
            }

            if(i == 0) {
                sb.append((char) b);
            }
            else {
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
    public void onPause() {
        if(!arduino.isOpen()) {return;}
        writeBytes(TURN_OFF_ALL_NOTES);
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        if(!arduino.isOpen()) {return;}

        writeBytes(TURN_OFF_ALL_NOTES);
        onSustainPedal(new SustainPedalEvent(false), timestamp); //weird but works.
    }

    @Override
    public void onSongFinished(long timestamp) {
        if(!arduino.isOpen()) {return;}

        writeBytes(TURN_OFF_ALL_NOTES);
        onSustainPedal(new SustainPedalEvent(false), timestamp); //weird but works.
    }

    private void writeBytes(byte[] data) {
        logger.debug("Sending " + byteArrayToStringColored(data));
        arduino.writeBytes(data, data.length);
    }

    @Override
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {
        boolean isOn = event.isOn();

        if(isOn) {
            writeBytes(new byte[] { 'S', (byte) 255});
        }
        else {
            writeBytes(new byte[] { 'S', 0});
        }

    }
}
