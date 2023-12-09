package plu.capstone.playerpiano.sheetmusic.serializable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.PianorollFileParser;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public abstract class SheetMusicFileWriter {

    protected final Logger LOGGER = new Logger(new Logger(PianorollFileParser.class), this.getClass().getSimpleName());

    public abstract SheetMusic readSheetMusic(BufferedInputStream in) throws IOException;
    public abstract void writeSheetMusic(BufferedOutputStream out, SheetMusic sheetMusic) throws IOException;

    protected static final void writeByte(BufferedOutputStream out, byte num) throws IOException {
        out.write(num);
    }

    protected static final byte readByte(BufferedInputStream in) throws IOException {
        int read = in.read();

        if(read == -1) {
            throw new EOFException("We reached the end of the file before we were suppose to!");
        }

        return (byte) read;
    }

    protected static final void writeBoolean(BufferedOutputStream out, boolean bool) throws IOException {
        writeByte(out, (byte) (bool ? 1 : 0));
    }

    protected static final boolean readBoolean(BufferedInputStream in) throws IOException {
        return readByte(in) == 1;
    }

    protected static final void writeInt(BufferedOutputStream out, int num) throws IOException {
        out.write(num >> 24);
        out.write(num >> 16);
        out.write(num >> 8);
        out.write(num);
    }

    protected static final int readInt(BufferedInputStream in) throws IOException {
        int num = 0;
        num |= in.read() << 24;
        num |= in.read() << 16;
        num |= in.read() << 8;
        num |= in.read();
        return num;
    }

    protected static final void writeLong(BufferedOutputStream out, long num) throws IOException {
        out.write((int) (num >> 56));
        out.write((int) (num >> 48));
        out.write((int) (num >> 40));
        out.write((int) (num >> 32));
        out.write((int) (num >> 24));
        out.write((int) (num >> 16));
        out.write((int) (num >> 8));
        out.write((int) num);
    }

    protected static final  long readLong(BufferedInputStream in) throws IOException {
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

    protected static final void writeString(BufferedOutputStream out, String str) throws IOException {

        if(str.length() > Integer.MAX_VALUE - 1) {
            throw new RuntimeException("String must be less than 255 characters");
        }

        writeInt(out, str.length());
        out.write(str.getBytes());

    }

    protected static final String readString(BufferedInputStream in) throws IOException {
        int stringLength = readInt(in);
        byte[] buffer = new byte[stringLength];
        in.read(buffer, 0, stringLength);
        return new String(buffer);
    }

    protected static final void writeEnum(BufferedOutputStream out, Enum value) throws IOException {
        writeInt(out, value.ordinal());
    }

    protected static final <T extends Enum<T>> T readEnum(BufferedInputStream in, Class<T> clazz) throws IOException {
        int ordinal = readInt(in);
        return clazz.getEnumConstants()[ordinal];
    }

    protected static final void writeEnumsBitwiseByte(BufferedOutputStream out, Enum[] values) throws IOException {
        byte num = 0;
        for(Enum value : values) {
            num |= 1 << (byte)value.ordinal();
        }
        writeByte(out, num);
    }

    protected static final <T extends Enum<T>> Set<T> readEnumsBitwiseByte(BufferedInputStream in, Class<? extends Enum> clazz) throws IOException {
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
