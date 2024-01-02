import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.commands.SubCommandParseMidi;
import plu.capstone.playerpiano.commands.SubCommandRunServer;
import plu.capstone.playerpiano.commands.SubCommandSongDBVerification;

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

        cmd.execute(args);
    }

}