import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.miscprograms.viewpianofile.SubCommandCreatePianoRollGraphics;
import plu.capstone.playerpiano.miscprograms.miditopianofile.SubCommandParseMidi;
import plu.capstone.playerpiano.miscprograms.maincontroller.SubCommandRunServer;
import plu.capstone.playerpiano.miscprograms.songdbverification.SubCommandSongDBVerification;

public class Main implements Callable<Integer> {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        cmd.addSubcommand("run-server", new SubCommandRunServer());
        cmd.addSubcommand("parse-midi", new SubCommandParseMidi());
        cmd.addSubcommand("songdb-verification", new SubCommandSongDBVerification());
        cmd.addSubcommand("create-piano-img", new SubCommandCreatePianoRollGraphics());

        cmd.execute(args);
    }

}