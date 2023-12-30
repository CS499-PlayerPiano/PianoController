import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.lang.reflect.Field;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.midiconverter.MainMidiConverter;
import plu.capstone.playerpiano.miscprograms.songdbverification.SongDBVerification;

public class Main implements Runnable {


    @Parameters(commandDescription = "Converts a midi file to a player piano file")
    public class MidiConverterOptions {

        @Parameter(names = {"-i", "--input"}, description = "The input midi file", required = true)
        private String input;

        @Parameter(names = {"-o", "--output"}, description = "The output player piano file", required = true)
        private String output;

    }

    @Parameters(commandDescription = "Verifies the songDB")
    public class SongDBVerificationOptions {

        @Parameter(names = {"-ga", "--github-action"}, description = "Whether or not this is running as a github action", required = true)
        private Boolean isGithubAction = null;

    }

    private MidiConverterOptions midiConverterOptions = new MidiConverterOptions();
    private SongDBVerificationOptions songDBVerificationOptions = new SongDBVerificationOptions();

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
                .addCommand("--songdb-verification", main.songDBVerificationOptions)
                .build();

        main.jCommander.parse(args);
        main.run();

    }

    @Override
    public void run() {
        System.out.println("Running");

        final boolean shouldWeParseAMidiFile = midiConverterOptions != null && midiConverterOptions.input != null && midiConverterOptions.output != null;
        final boolean songDBVerification = songDBVerificationOptions != null && songDBVerificationOptions.isGithubAction != null;

        //error out if we don't have any options
        if(!shouldWeParseAMidiFile && !runServer && !songDBVerification) {
            System.err.println("You must specify either --run-server, --parse-midi, or --run-songdb-verification");
            jCommander.usage();
            return;
        }

        //check if more than one option is specified
        if((shouldWeParseAMidiFile ? 1 : 0) + (runServer ? 1 : 0) + (songDBVerification ? 1 : 0) > 1) {
            System.err.println("You can only specify one option at a time!");
            System.err.println("run-server: " + runServer);
            System.err.println("parse-midi: " + shouldWeParseAMidiFile);
            System.err.println("songdb-verification: " + songDBVerification);
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

        if(songDBVerification) {
            System.out.println("Running SongDB Verification.....");
            new SongDBVerification(songDBVerificationOptions.isGithubAction).run();
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

//    static boolean checkOnlyOneOptionIsSelected(Class<?> clazz, Object instance) throws IllegalAccessException {
//
//        int total = 0;
//
//        for(Field field : clazz.getDeclaredFields()) {
//            if(field.getType() == boolean.class) {
//
//                Parameter p = field.getAnnotation(Parameter.class);
//                boolean value = field.getBoolean(instance);
//
//                System.out.println("Field: " + field.getName() + " Value: " + value);
//                System.out.println("  - Parameter: " + p);
//
//                total += value ? 1 : 0;
//            }
////            else if(field.getType() == Object.class) {
////                checkOnlyOneOptionIsSelected(field.getType(), field.get(instance));
////            }
////            else if(field.getType() == String.class) {
////
////                Parameter p = field.getAnnotation(Parameter.class);
////                String value = (String) field.get(instance);
////
////                total += (value != null && !value.isEmpty()) ? 1 : 0;
////            }
//
//
//        }
//
//        return total > 1;
//
//    }
}