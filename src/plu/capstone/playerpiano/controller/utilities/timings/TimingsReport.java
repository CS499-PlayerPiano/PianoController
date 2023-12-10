package plu.capstone.playerpiano.controller.utilities.timings;

import java.util.ArrayList;
import java.util.List;
import plu.capstone.playerpiano.logger.Logger;

public class TimingsReport {

    private final Logger LOGGER = new Logger(this);

    private List<Timing> timings = new ArrayList<>();

    public Timing newTiming(String name) {
        Timing timing = new Timing(name);
        timings.add(timing);
        timing.start();
        return timing;
    }

    private long getTotalTime() {
        long total = 0;
        for(Timing timing : timings) {
            total += timing.getDuration();
        }
        return total;
    }

    public void printReport() {
        LOGGER.info("--- Timings Report ---");
        LOGGER.info("Total time: " + getTotalTime() + "ms");
        LOGGER.info("Steps breakdown:");
        for(Timing timing : timings) {
            LOGGER.info("  - Step \"" + timing.getName() + "\" took " + timing.getDuration() + "ms");
        }
        LOGGER.info("--- End Timings Report ---");
    }

}
