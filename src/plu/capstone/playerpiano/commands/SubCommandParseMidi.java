package plu.capstone.playerpiano.commands;

import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.miscprograms.miditopianofile.MidiCleaner;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

@Command(name = "parse-midi", description = "Converts a midi file to a player piano file")
public class SubCommandParseMidi implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, description = "The input midi file", required = true)
    private File input;

    @Option(names = {"-o", "--output"}, description = "The output player piano file", required = true)
    private File output;

    @Option(names = {"-v", "--version"}, description = "The version of the output file", required = false, defaultValue = "" + SheetMusicReaderWriter.LATEST_VERSION)
    private int version;

    @Override
    public Integer call() throws Exception {
        System.out.println("Parsing Midi.....");

        if(input == null) {
            System.err.println("Input file not specified. Please specify a valid file using -i or --input");
            return 1;
        }

        if(output == null) {
            System.err.println("Output file not specified. Please specify a valid file using -o or --output");
            return 1;
        }

        if(input == null || !input.exists()) {
            System.out.println(input.getAbsolutePath());
            System.err.println("Input file does not exist on the file system! Please specify a valid file using -i or --input");
            return 1;
        }

        if(version < 1 || version > SheetMusicReaderWriter.LATEST_VERSION) {
            System.err.println("Invalid version number");
            System.err.println("Valid versions are 1-" + SheetMusicReaderWriter.LATEST_VERSION);
            return 1;
        }

        new MidiCleaner(input, output, version).run();
        return 0;
    }
}
