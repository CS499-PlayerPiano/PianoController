package plu.capstone.playerpiano.sheetmusic.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class BufferedPianoFileWriter {

    private final BufferedOutputStream out;

    public BufferedPianoFileWriter(File pianoFile) throws IOException {
        this.out = new BufferedOutputStream(Files.newOutputStream(pianoFile.toPath()));
    }

    public void writeByte(byte num, String whatIsItFor) throws IOException {
        out.write(num);
    }

    public void writeBytes(byte[] bytes, String whatIsItFor) throws IOException {
        out.write(bytes);
    }

    public void writeBoolean(boolean bool, String whatIsItFor) throws IOException {
        out.write((byte) (bool ? 1 : 0));
    }

    public void writeInt(int num, String whatIsItFor) throws IOException {
        out.write(num >> 24);
        out.write(num >> 16);
        out.write(num >> 8);
        out.write(num);
    }

    public void writeLong(long num, String whatIsItFor) throws IOException {
        out.write((int) (num >> 56));
        out.write((int) (num >> 48));
        out.write((int) (num >> 40));
        out.write((int) (num >> 32));
        out.write((int) (num >> 24));
        out.write((int) (num >> 16));
        out.write((int) (num >> 8));
        out.write((int) num);
    }

    public void writeShort(short num, String whatIsItFor) throws IOException {
        out.write(num >> 8);
        out.write(num);
    }
    
    public void writeString(String str, String whatIsItFor) throws IOException {

        if(str.length() > Integer.MAX_VALUE - 1) {
            throw new RuntimeException("String must be less than 255 characters");
        }

        writeInt(str.length(), whatIsItFor);
        writeBytes(str.getBytes(), whatIsItFor);

    }

    public void writeEnumByte(Enum value, String whatIsItFor) throws IOException {
        writeByte((byte)value.ordinal(), whatIsItFor);
    }
    public void writeEnumsBitwiseByte(Enum[] values, String whatIsItFor) throws IOException {
        byte num = 0;
        for(Enum value : values) {
            num |= 1 << (byte)value.ordinal();
        }
        writeByte(num, whatIsItFor);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

}
