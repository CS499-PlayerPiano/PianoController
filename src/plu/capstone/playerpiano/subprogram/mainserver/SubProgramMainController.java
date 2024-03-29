package plu.capstone.playerpiano.subprogram.mainserver;

import java.util.Set;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.subprogram.SubProgram;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class SubProgramMainController extends SubProgram {

    private final Logger logger = new Logger(this);

    private QueueManager queueManager = new QueueManager(this);
    @Getter private JavalinWebServerOutput webServerOutput = new JavalinWebServerOutput(queueManager);

    @Override
    public String getSubCommand() {
        return "run-server";
    }

    @Override
    protected void addProgramSpecificOutputPlugins(Set<Output> outputs) {
        logger.info("Adding web server output plugin");
        outputs.add(webServerOutput);
    }

    @Override
    public void run() {
        logger.info("PlayerPianoController running!");

        queueManager.start();

//        webServerOutput.start();

    }



}
