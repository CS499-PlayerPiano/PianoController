package plu.capstone.playerpiano.utilities;

import lombok.Getter;

public class Stopwatch {

    private long startTime = 0;
    private long stopTime = 0;

    @Getter
    private boolean running = false;


    public void start() {
        if (running) {return;}
        this.startTime = System.nanoTime() - getElapsedTimeNS();
        this.running = true;
    }


    public void stop() {
        if (!running) {return;}
        this.stopTime = System.nanoTime();
        this.running = false;
    }


    public long getElapsedTimeNS() {
        long elapsed;
        if (running) {
            elapsed = (System.nanoTime() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }

    public long getElapsedTimeMS() {
        return getElapsedTimeNS() / 1_000_000;
    }
}
