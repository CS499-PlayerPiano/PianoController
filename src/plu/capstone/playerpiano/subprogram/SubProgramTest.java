package plu.capstone.playerpiano.subprogram;

import picocli.CommandLine.Option;

public class SubProgramTest extends SubProgram {

    @Option(names = {"-f", "--foo"}, description = "foo option", required = true, defaultValue = "false")
    private String foo;

    @Override
    public String getSubCommand() {
        return "run-server";
    }

    @Override
    public void run() {
        System.out.println("Running Test SubProgram...");
    }

}
