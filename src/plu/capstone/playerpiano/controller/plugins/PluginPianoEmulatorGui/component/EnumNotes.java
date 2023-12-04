package plugins.PluginPianoEmulatorGui.component;

import lombok.Getter;

/**
 * Enum for the notes on the piano.
 * Contains the cut points for the key shapes.
 */
public enum EnumNotes {

    A(7 / 24f, 3 / 24f),
    B(11 / 24f, 0),
    C(0, 9 / 24f),
    D(5 / 24f, 5 / 24f),
    E(9 / 24f, 0),
    F(0, 11 / 24f),
    G(3 / 24f, 7 / 24f);

    @Getter private final float cutLeft, cutRight;
    EnumNotes(float cutLeft, float cutRight) {
        this.cutLeft = cutLeft;
        this.cutRight = cutRight;
    }

    public EnumNotes next() {
        return values()[(ordinal() + 1) % values().length];
    }
}