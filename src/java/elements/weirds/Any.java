package elements.weirds;

import elements.Element;

public class Any extends Element {
    public Any() {
        NAME("Any");
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Element)) {
            return false;
        }

        final Element element = (Element) obj;
        return element.getName() != null;
    }
}
