package elements.liquids;

import elements.Element;
import toolbox.Color;

public class Water extends Element {
    public Water() {
        NAME("Water");
        KEY("3");
        COLOR(new Color(0.1f, 0.5f, 1, 0.5f));

        WATER();
    }
}
