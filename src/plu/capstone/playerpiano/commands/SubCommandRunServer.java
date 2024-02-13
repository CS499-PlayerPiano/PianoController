package plu.capstone.playerpiano.commands;

import picocli.CommandLine.Command;
import plu.capstone.playerpiano.miscprograms.maincontroller.PlayerPianoController;

@Command(name = "run-server", description = "Runs the piano controller software")
public class SubCommandRunServer implements Runnable {

    @Override
    public void run() {
        System.out.println("Running Server.....");
        PlayerPianoController.getInstance().run();
    }

}
