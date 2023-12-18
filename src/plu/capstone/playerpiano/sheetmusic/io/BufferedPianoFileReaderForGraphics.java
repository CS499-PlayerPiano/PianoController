package plu.capstone.playerpiano.sheetmusic.io;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParser.WhatIsItFor;

public class BufferedPianoFileReaderForGraphics extends BufferedPianoFileReader {

    @Getter
    private Queue<NameAndByte> nameAndBytes = new LinkedList<>();

    public BufferedPianoFileReaderForGraphics(File pianoFile) throws FileNotFoundException {
        super(pianoFile);
    }

    @Override
    public byte readByte(WhatIsItFor whatIsItFor) throws IOException {
        byte result = super.readByte(whatIsItFor);
        nameAndBytes.add(new NameAndByte(whatIsItFor, result));
        return result;
    }

    @Getter
    public static class NameAndByte {

        private final String name;
        private final byte value;
        private final Color color;

        public NameAndByte(WhatIsItFor whatIsItFor, byte value) {
            this.name = whatIsItFor.getName();
            this.value = value;
            this.color = new Color(whatIsItFor.getHexColor());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NameAndByte that = (NameAndByte) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        //value as a String 00-FF
        public String getValueAsString() {
            return String.format("%02X", value);
        }
    }
}
