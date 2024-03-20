import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.programs.maincontroller.SubCommandRunServer;
import plu.capstone.playerpiano.programs.songdbverification.SubCommandSongDBVerification;

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
        cmd.addSubcommand("songdb-verification", new SubCommandSongDBVerification());

        cmd.execute(args);
    }

}