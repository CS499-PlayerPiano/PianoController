import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.midiconverter.MainMidiConverter;

public class Main implements Runnable {


    @Parameters(commandDescription = "Converts a midi file to a player piano file")
    public class MidiConverterOptions {

        @Parameter(names = {"-i", "--input"}, description = "The input midi file", required = true)
        private String input;

        @Parameter(names = {"-o", "--output"}, description = "The output player piano file", required = true)
        private String output;

    }

    private MidiConverterOptions midiConverterOptions = new MidiConverterOptions();

    @Parameter(names = {"--run-server"}, description = "Runs the piano controller software")
    private boolean runServer = false;

    @Parameter(names = "--help", help = true, description = "Shows the help message")
    private boolean help = false;

    private JCommander jCommander;
    public static void main(String[] args) {

        Main main = new Main();
        main.jCommander = JCommander.newBuilder()
                .addObject(main)
                .programName("Player Piano")
                .addCommand("--parse-midi", main.midiConverterOptions)
                .build();

        main.jCommander.parse(args);
        main.run();

    }

    @Override
    public void run() {
        System.out.println("Running");
        final boolean shouldWeParseAMidiFile = midiConverterOptions != null && midiConverterOptions.input != null && midiConverterOptions.output != null;

        //error out if we don't have any options
        if(!shouldWeParseAMidiFile && !runServer) {
            System.err.println("You must specify either --run-server or --parse-midi");
            jCommander.usage();
            return;
        }

        // Error out if we are trying to parse a midi file and run the server at the same time
        if(shouldWeParseAMidiFile && runServer) {
            System.err.println("You can't parse a midi file and run the server at the same time!");
            jCommander.usage();
            return;
        }

        //Show the help message if we need to
        if(this.help) {
            jCommander.usage();
            return;
        }

        if(runServer) {
            System.out.println("Running Server.....");
            PlayerPianoController.getInstance().run();
            return;
        }

        if(shouldWeParseAMidiFile) {
            System.out.println("Parsing Midi File.....");
            try {
                new MainMidiConverter().run(midiConverterOptions.input, midiConverterOptions.output);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

//        System.out.println("Debug: " + debug);
//        System.out.println("Run Server: " + runServer);
//        System.out.println("Parse Midi File: " + shouldWeParseAMidiFile);
//        if(midiConverterOptions != null) {
//            System.out.println("  - Midi Converter Options: " + midiConverterOptions.input + " " + midiConverterOptions.output);
//        }


        //PlayerPianoController.getInstance().run();
    }
}