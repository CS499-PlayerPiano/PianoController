package plu.capstone.playerpiano.subprogram;

import picocli.CommandLine.Option;

public class SubProgramTest extends SubProgram {

    @Option(names = {"-f", "--foo"}, description = "foo option", required = true)
    private String foo;

    @Override
    public String getSubCommand() {
        return "test";
    }

    @Override
    public void run() {
        System.out.println("Running Test SubProgram...");
    }

}
