package plu.capstone.playerpiano.utilities.timings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import plu.capstone.playerpiano.logger.Logger;

public class TimingsReport {

    private final Logger LOGGER = new Logger(this);

    private List<Timing> allTimings = new ArrayList<>();
    private Queue<Timing> stopQueue = new LinkedList<>();

    public void start(String name) {
        Timing timing = new Timing(name);
        stopQueue.add(timing);
        allTimings.add(timing);
        timing.start();
    }

    public void stop() {
        Timing timing = stopQueue.poll();
        if(timing == null) {
            LOGGER.error("Tried to stop a timing that was never pushed! Did you forget to start a timing?");
            return;
        }
        timing.stop();
    }

    private long getTotalTime() {
        long total = 0;
        for(Timing timing : allTimings) {
            total += timing.getDuration();
        }
        return total;
    }

    public void printReport() {
        LOGGER.info("--- Timings Report ---");
        LOGGER.info("Total time: " + getTotalTime() + "ms");
        LOGGER.info("Steps breakdown:");
        for(Timing timing : allTimings) {
            LOGGER.info("  - Step \"" + timing.getName() + "\" took " + timing.getDuration() + "ms");
        }
        LOGGER.info("--- End Timings Report ---");
    }

}
