package plu.capstone.playerpiano.programs.songdbverification;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "parse-midi", description = "Converts a midi file to a player piano file")
public class SubCommandSongDBVerification implements Callable<Integer> {

    @Option(names = {"-ga", "--github-action"}, description = "Is this a github action?", required = false, defaultValue = "false")
    private boolean githubAction;

    @Override
    public Integer call() throws Exception {
        System.out.println("Parsing Midi.....");
        return new SongDBVerification(githubAction).call();
    }
}
