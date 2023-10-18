package plu.capstone.playerpiano.controller.utilities;

/**
 * A collection of math utilities.
 */
public class MathUtilities {

    private MathUtilities() {}

    /**
     * Maps a value from one range to another.
     * @param x the value to map
     * @param in_min the minimum value of the input range
     * @param in_max the maximum value of the input range
     * @param out_min the minimum value of the output range
     * @param out_max the maximum value of the output range
     * @return the mapped value
     */
    public static final int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}
