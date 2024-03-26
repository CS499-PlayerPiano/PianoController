import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.subprogram.SubProgram;
import plu.capstone.playerpiano.subprogram.SubProgramTest;
import plu.capstone.playerpiano.subprogram.keydebug.SubProgramKeyDebug;
import plu.capstone.playerpiano.subprogram.mainserver.SubProgramMainController;
import plu.capstone.playerpiano.subprogram.mappinggenerator.SubProgramMappingGenerator;
import plu.capstone.playerpiano.subprogram.midiviewer.SubProgramMidiViewer;
import plu.capstone.playerpiano.subprogram.songdb.SubProgramSongDBVerification;

public class Main implements Callable<Integer> {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    private static final SubProgram SUB_PROGRAMS[] = {
            new SubProgramMainController(),
            new SubProgramSongDBVerification(),
            new SubProgramMidiViewer(),
            new SubProgramTest(),
            new SubProgramMappingGenerator(),
            new SubProgramKeyDebug()
    };

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());

        for(SubProgram subProgram : SUB_PROGRAMS) {
            cmd.addSubcommand(subProgram.getSubCommand(), subProgram);
        }

        cmd.execute(args);
    }

}