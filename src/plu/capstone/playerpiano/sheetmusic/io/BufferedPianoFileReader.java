package plu.capstone.playerpiano.sheetmusic.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BufferedPianoFileReader {

    private final BufferedInputStream in;

    public BufferedPianoFileReader(File pianoFile) throws FileNotFoundException {
        this.in = new BufferedInputStream(new FileInputStream(pianoFile));;
    }

    public byte readByte(String whatIsItFor) throws IOException {
        int read = in.read();

        return (byte) read;
    }

    public boolean readBoolean(String whatIsItFor) throws IOException {
        return in.read() == 1;
    }

    public int readInt(String whatIsItFor) throws IOException {
        int num = 0;
        num |= in.read() << 24;
        num |= in.read() << 16;
        num |= in.read() << 8;
        num |= in.read();
        return num;
    }

    public long readLong(String whatIsItFor) throws IOException {
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

    public short readShort(String whatIsItFor) throws IOException {
        short num = 0;
        num |= in.read() << 8;
        num |= in.read();
        return num;
    }
    
    public String readString(String whatIsItFor) throws IOException {
        int stringLength = readInt(whatIsItFor);
        byte[] buffer = new byte[stringLength];
        in.read(buffer, 0, stringLength);
        return new String(buffer);
    }
    public <T extends Enum<T>> T readEnumByte(Class<T> clazz, String whatIsItFor) throws IOException {
        int ordinal = readByte(whatIsItFor);
        return clazz.getEnumConstants()[ordinal];
    }

    public <T extends Enum<T>> Set<T> readEnumsBitwiseByte(Class<? extends Enum> clazz, String whatIsItFor) throws IOException {
        byte num = (byte) in.read();

        Set<T> values = new HashSet<>();

        for(int i = 0; i < 8; ++i) {
            if((num & (1 << i)) != 0) {
                values.add((T) clazz.getEnumConstants()[i]);
            }
        }
        return values;
    }

    public void close() throws IOException {
        in.close();
    }
}
