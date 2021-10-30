package elements.solids.movables;

import elements.Element;
import toolbox.Color;

public class Sand extends Element {
    public Sand() {
        NAME("Sand");
        KEY("1");
        COLOR(new Color(1, 0.6f, 0.25f, 1));

        SAND();
    }
}
