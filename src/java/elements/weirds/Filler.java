package elements.weirds;

import elements.Element;
import toolbox.Color;

public class Filler extends Element {
    public Filler() {
        NAME("Filler");
        KEY("9");
        COLOR(new Color(0.1f, 0.1f, 0.1f, 1));

//        X("T*", "TT", 0, 1);
//        X("T/*", "T/T", 1, 1);
//        Z("^*/T", "^T/T", 1, 1);
//        Z("T*", "TT", 0, 1);
    }
}