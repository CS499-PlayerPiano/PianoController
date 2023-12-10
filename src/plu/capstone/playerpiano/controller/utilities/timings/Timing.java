package plu.capstone.playerpiano.controller.utilities.timings;

import lombok.Getter;

@Getter
public class Timing {

    private final String name;
    private long startTime;
    private long endTime;

    protected Timing(String name) {
        this.name = name;
    }

    public Timing start() {
        startTime = System.currentTimeMillis();
        return this;
    }

    public Timing stop() {
        endTime = System.currentTimeMillis();
        return this;
    }

    public long getDuration() {
        return endTime - startTime;
    }

}
