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
import plu.capstone.playerpiano.outputs.arduino.packets.Packet;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketNotes;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketNotes_B;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketNotes_M;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketNotes_N;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketResetPower;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketSustainPedal;
import plu.capstone.playerpiano.outputs.arduino.packets.PacketTurnOffAllNotes;
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
    private boolean sendPowerResetAfterEverySong = true;

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
        sendPowerResetAfterEverySong = getConfig().getBoolean("sendPowerResetAfterEverySong");

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

        //We don't want to send a packet if there are no notes to play.
        if(notes.size() == 0) {
            return;
        }

        //We have three different packets we can send to the arduino, a note packet and two different batch note packets.
        //Depending on the size of the packet, we will send the smaller one.
        //It just depends how many notes and the type are hit.
        PacketNotes_N packetN = new PacketNotes_N(notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);
        PacketNotes_M packetM = new PacketNotes_M(notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);
        PacketNotes_B packetB = new PacketNotes_B(notes, noteMapping, velocityMappingMin, velocityMappingMax, ignoreVelocity);


        String whichPacket = "N";

        PacketNotes packetToBeSent = packetN;
        if(packetB.size() < packetN.size()) {
            packetToBeSent = packetB;
            whichPacket = "B";
        }

        if(packetM.isValid() && packetM.size() < packetToBeSent.size()) {
            packetToBeSent = packetM;
            whichPacket = "M";
        }

//        logger.debug("Sending " + whichPacket + " packet: len=" + dataToBeWritten.length);

        writePacket(packetToBeSent);

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

        writeRawBytes(buffer.array());
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
        writePacket(new PacketTurnOffAllNotes());
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        writePacket(new PacketSustainPedal(false));
        writePacket(new PacketTurnOffAllNotes());
    }

    @Override
    public void onSongFinished(long timestamp) {
        writePacket(new PacketSustainPedal(false));
        writePacket(new PacketTurnOffAllNotes());
        writePacket(new PacketResetPower());
    }

    private void writePacket(Packet packet) {
        final byte[] data = packet.getBytes();
        if(data == null) {return;}
        writeRawBytes(data);
    }

    private void writeRawBytes(byte[] data) {
        if(arduino == null) {return;}
        if(!arduino.isOpen()) {return;}
        if(data == null || data.length == 0) {return;}
        arduino.writeBytes(data, data.length);
    }

    @Override
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {
        final boolean isOn = event.isOn();
        writePacket(new PacketSustainPedal(isOn));
    }
}
