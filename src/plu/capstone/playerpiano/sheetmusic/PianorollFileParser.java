package plu.capstone.playerpiano.sheetmusic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import plu.capstone.playerpiano.logger.Logger;

public class PianorollFileParser {

    private static final Logger LOGGER = new Logger(PianorollFileParser.class);

    //Test read and write
    public static void main(String[] args) throws IOException {
        SheetMusic orig = new SheetMusic();
        orig.songLengthMS = 1000;
        orig.putNote(0, new Note(
                (byte) 68,
                (byte) 128,
                true,
                1
        ));
        orig.putNote(6, new Note(
                (byte) 68,
                (byte) 0,
                false,
                1
        ));

        File file = new File("tmp/test.pianoroll");
        translateSheetMusicToPiannoroll(orig, file, 1);
        SheetMusic newSheetMusic = translatePianorollToSheetMusic(file);

        System.out.println(orig.equals(newSheetMusic));
    }

    public static SheetMusic translatePianorollToSheetMusic(File pianoRollFile) throws IOException {

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(pianoRollFile));

        final int version = readInt(in);

        SheetMusic sheetMusic = new SheetMusic();

        /*
        Version 1:
         */
        if(version == 1) {
            //length in ms
            sheetMusic.songLengthMS = readLong(in);

            //number of timeslots
            final int numTimeslots = readInt(in);

            //for each note
            for(int i = 0; i < numTimeslots; ++i) {
                //time
                long time = readLong(in);

                //number of notes at this time
                int numNotesAtTime = readInt(in);

                //for each note at this time
                for(int j = 0; j < numNotesAtTime; ++j) {

                    final byte keyNumber = readByte(in);
                    final byte velocity = readByte(in);
                    final boolean noteOn = readBoolean(in);
                    final byte channelNum = readByte(in);

                    Note note = new Note(
                            keyNumber,
                            velocity,
                            noteOn,
                            channelNum
                    );

                    sheetMusic.putNote(time, note);
                }
            }
        }
        else {
            LOGGER.error("Error reading file. Unknown version: " + version);
        }

        Test test = readEnum(in, Test.class);

        in.close();

        return sheetMusic;
    }

    enum Test {
        ONE
    }

    public static void translateSheetMusicToPiannoroll(SheetMusic sheetMusic, File pianoRollFile, int version) throws IOException {

        BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pianoRollFile.toPath()));

        //version
        writeInt(out, version);

        /*
        Version 1:
        - long SongLengthMS
        - int number of timeslots
        - for each timeslot:
            - long time
            - int number of notes at this time
            - for each note at this time:
                - byte keyNumber
                - byte velocity
                - boolean noteOn
                - byte channelNum
         */
        if(version == 1) {

            //song length
            writeLong(out, sheetMusic.getSongLengthMS());

            //number of timeslots
            writeInt(out, sheetMusic.getNoteMap().size());

            //for each timeslot
            for(Map.Entry<Long, List<Note>> entry : sheetMusic.getNoteMap().entrySet()) {
                //time
                writeLong(out, entry.getKey());

                //number of notes at this time
                writeInt(out, entry.getValue().size());

                //for each note at this time
                for(Note note : entry.getValue()) {
                    writeByte(out, (byte) note.getKeyNumber());
                    writeByte(out, (byte) note.getVelocity());
                    writeBoolean(out, note.isNoteOn());
                    writeByte(out, (byte) note.getChannelNum());
                }
            }

        }
        else {
            LOGGER.error("Error writing file. Unknown version: " + version);
        }

        out.flush();
        out.close();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void writeByte(BufferedOutputStream out, byte num) throws IOException {
        out.write(num);
    }

    private static byte readByte(BufferedInputStream in) throws IOException {
        int read = in.read();

        if(read == -1) {
            throw new EOFException("We reached the end of the file before we were suppose to!");
        }

        return (byte) read;
    }

    private static void writeBoolean(BufferedOutputStream out, boolean bool) throws IOException {
        writeByte(out, (byte) (bool ? 1 : 0));
    }

    private static boolean readBoolean(BufferedInputStream in) throws IOException {
        return readByte(in) == 1;
    }

    private static void writeInt(BufferedOutputStream out, int num) throws IOException {
        out.write(num >> 24);
        out.write(num >> 16);
        out.write(num >> 8);
        out.write(num);
    }

    private static int readInt(BufferedInputStream in) throws IOException {
        int num = 0;
        num |= in.read() << 24;
        num |= in.read() << 16;
        num |= in.read() << 8;
        num |= in.read();
        return num;
    }

    private static void writeLong(BufferedOutputStream out, long num) throws IOException {
        out.write((int) (num >> 56));
        out.write((int) (num >> 48));
        out.write((int) (num >> 40));
        out.write((int) (num >> 32));
        out.write((int) (num >> 24));
        out.write((int) (num >> 16));
        out.write((int) (num >> 8));
        out.write((int) num);
    }

    private static  long readLong(BufferedInputStream in) throws IOException {
        long num = 0;
        num |= (long) in.read() << 56;
        num |= (long) in.read() << 48;
        num |= (long) in.read() << 40;
        num |= (long) in.read() << 32;
        num |= (long) in.read() << 24;
        num |= (long) in.read() << 16;
        num |= (long) in.read() << 8;
        num |= (long) in.read();
        return num;
    }

    private static void writeString(BufferedOutputStream out, String str) throws IOException {

        if(str.length() > Integer.MAX_VALUE - 1) {
            throw new RuntimeException("String must be less than 255 characters");
        }

        writeInt(out, str.length());
        out.write(str.getBytes());

    }

    private static String readString(BufferedInputStream in) throws IOException {
        int stringLength = readInt(in);
        byte[] buffer = new byte[stringLength];
        in.read(buffer, 0, stringLength);
        return new String(buffer);
    }

    private static void writeEnum(BufferedOutputStream out, Enum value) throws IOException {
        writeInt(out, value.ordinal());
    }

    private static <T extends Enum<T>> T readEnum(BufferedInputStream in, Class<T> clazz) throws IOException {
        int ordinal = readInt(in);
        return clazz.getEnumConstants()[ordinal];
    }



    private static void writeEnumsBitwiseByte(BufferedOutputStream out, Enum[] values) throws IOException {
        byte num = 0;
        for(Enum value : values) {
            num |= 1 << (byte)value.ordinal();
        }
        writeByte(out, num);
    }

    private static <T extends Enum<T>> Set<T> readEnumsBitwiseByte(BufferedInputStream in, Class<? extends Enum> clazz) throws IOException {
        byte num = readByte(in);

        Set<T> values = new HashSet<>();

        for(int i = 0; i < 8; ++i) {
            if((num & (1 << i)) != 0) {
               values.add((T) clazz.getEnumConstants()[i]);
            }
        }
        return values;
    }

}
