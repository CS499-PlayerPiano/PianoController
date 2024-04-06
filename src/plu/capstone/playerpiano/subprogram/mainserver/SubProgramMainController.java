package plu.capstone.playerpiano.subprogram.mainserver;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.subprogram.mainserver.statisticsdb.OutputStatisticsDB;
import plu.capstone.playerpiano.subprogram.SubProgram;
import plu.capstone.playerpiano.subprogram.mainserver.statisticsdb.RawStatistics;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class SubProgramMainController extends SubProgram {

    private final Logger logger = new Logger(this);

    @Getter private QueueManager queueManager = new QueueManager(this);
    @Getter private JavalinWebServerOutput webServerOutput = new JavalinWebServerOutput(this);
    @Getter private RawStatistics statistics = new RawStatistics();
    private OutputStatisticsDB outputStatistics = new OutputStatisticsDB(this);

    //TODO: Make settings file
    private static final long SAVE_INTERVAL = 1000 * 60 * 1; // 1 MINUTE
    private static final int SEND_TO_WEBSITE_INTERVAL = 100; //milliseconds

    @Override
    public String getSubCommand() {
        return "run-server";
    }

    @Override
    protected void addProgramSpecificOutputPlugins(Set<Output> outputs) {
        logger.info("Adding web server output plugin");
        outputs.add(webServerOutput);
        outputs.add(outputStatistics);
    }

    @Override
    public void run() {
        logger.info("PlayerPianoController running!");

        //Load the statistics from the DB
        statistics.loadConfig();

        //Save the statistics to the DB every SAVE_INTERVAL
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.info("Saving statistics to DB...");
                statistics.saveConfig();
            }
        }, SAVE_INTERVAL, SAVE_INTERVAL);

        queueManager.start();



//        webServerOutput.start();

    }

}
