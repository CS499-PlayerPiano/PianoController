package plu.capstone.playerpiano.utilities;

import lombok.Getter;

public class Stopwatch {

    private long startTime = 0;
    private long stopTime = 0;

    @Getter
    private boolean running = false;


    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.nanoTime();
        this.running = false;
    }


    public long getElapsedTime() {
        long elapsed;
        if (running) {
            elapsed = (System.nanoTime() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }
}
