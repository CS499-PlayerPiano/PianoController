package plu.capstone.playerpiano.programs.maincontroller;

import picocli.CommandLine.Command;

@Command(name = "run-server", description = "Runs the piano controller software")
public class SubCommandRunServer implements Runnable {

    @Override
    public void run() {
        System.out.println("Running Server.....");
        PlayerPianoController.getInstance().run();
    }

}
