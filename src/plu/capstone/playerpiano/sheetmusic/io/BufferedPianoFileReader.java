package plu.capstone.playerpiano.sheetmusic.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParser.WhatIsItFor;

public class BufferedPianoFileReader {

    private final BufferedInputStream in;

    public BufferedPianoFileReader(File pianoFile) throws FileNotFoundException {
        this.in = new BufferedInputStream(new FileInputStream(pianoFile));;
    }

    public byte readByte(WhatIsItFor whatIsItFor) throws IOException {
        int read = in.read();
        if(read == -1) {
            throw new IOException("Unexpected end of file while reading PianoFile!");
        }
        return (byte) read;
    }

    public void readBytes(byte[] buffer, WhatIsItFor whatIsItFor) throws IOException {
        int read = in.read(buffer);
        if(read == -1) {
            throw new IOException("Unexpected end of file while reading PianoFile!");
        }
    }

    public boolean readBoolean(WhatIsItFor whatIsItFor) throws IOException {
        return readByte(whatIsItFor) == 1;
    }

    public int readInt(WhatIsItFor whatIsItFor) throws IOException {
        int num = 0;
        num |= readByte(whatIsItFor) << 24;
        num |= readByte(whatIsItFor) << 16;
        num |= readByte(whatIsItFor) << 8;
        num |= readByte(whatIsItFor);
        return num;
    }

    public long readLong(WhatIsItFor whatIsItFor) throws IOException {
        long num = 0;
        num |= (long) readByte(whatIsItFor) << 56;
        num |= (long) readByte(whatIsItFor) << 48;
        num |= (long) readByte(whatIsItFor) << 40;
        num |= (long) readByte(whatIsItFor) << 32;
        num |= (long) readByte(whatIsItFor) << 24;
        num |= (long) readByte(whatIsItFor) << 16;
        num |= (long) readByte(whatIsItFor) << 8;
        num |= (long) readByte(whatIsItFor);
        return num;
    }

    public short readShort(WhatIsItFor whatIsItFor) throws IOException {
        short num = 0;
        num |= readByte(whatIsItFor) << 8;
        num |= readByte(whatIsItFor);
        return num;
    }
    
    public String readString(WhatIsItFor whatIsItFor) throws IOException {
        int stringLength = readInt(whatIsItFor);
        byte[] buffer = new byte[stringLength];
        readBytes(buffer, whatIsItFor);
        return new String(buffer);
    }
    public <T extends Enum<T>> T readEnumByte(Class<T> clazz, WhatIsItFor whatIsItFor) throws IOException {
        int ordinal = readByte(whatIsItFor);
        return clazz.getEnumConstants()[ordinal];
    }

    public <T extends Enum<T>> Set<T> readEnumsBitwiseByte(Class<? extends Enum> clazz, WhatIsItFor whatIsItFor) throws IOException {
        byte num = readByte(whatIsItFor);

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
