package plu.capstone.playerpiano.controller.utilities.graphics.piano;

import java.awt.Shape;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a key shape and whether it is a black or white key
 */
@Getter
@AllArgsConstructor
public class KeyShape {
    private final Shape shape;
    private final boolean black;
}
