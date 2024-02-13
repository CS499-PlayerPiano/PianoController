package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "create-piano-img", description = "Create an image from a .piano file")
public class SubCommandCreatePianoRollGraphics implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, description = "The input piano file", required = true)
    private File input;

    @Option(names = {"-o", "--output"}, description = "The output png file", required = true)
    private File output;

//    @Option(names = {"-fv", "--force-version"}, description = "Force the version of the piano file. This may lead to unexpected bugs!", required = false, defaultValue = "-1")
//    private int version;


    @Override
    public Integer call() throws Exception {
        System.out.println("Creating image.....");

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


        new PianoFileFormatGraphicsCreator(input, output).run();
        return 0;
    }
}
