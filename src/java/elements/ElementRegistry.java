package elements;

import com.sun.istack.internal.NotNull;
import elements.liquids.Water;
import elements.solids.movables.Dirt;
import elements.solids.movables.Light;
import elements.solids.movables.Sand;
import elements.weirds.Any;
import elements.weirds.Empty;
import elements.weirds.Filler;

import java.util.HashMap;

public class ElementRegistry {
    private static final HashMap<String, Element> nameToElement = new HashMap<>();
    private static final HashMap<Integer, String> idToName = new HashMap<>();
    public static int lastId = -1;

    public static final Element anyElement = new Any();
    public static final Element emptyElement = new Empty();

    public static void init() {
        ElementRegistry.register(ElementRegistry.anyElement);
        ElementRegistry.register(ElementRegistry.emptyElement);
        ElementRegistry.register(new Light());
        ElementRegistry.register(new Sand());
        ElementRegistry.register(new Dirt());
        ElementRegistry.register(new Water());
        ElementRegistry.register(new Filler());
    }

    private static void register(final Element entry) {
        final String name = entry.getName();

        if (ElementRegistry.nameToElement.containsKey(name)) {
            System.err.println("Tried to register same element more than once: " + name);
            return;
        }

        ElementRegistry.idToName.put(entry.getId(), name);
        ElementRegistry.nameToElement.put(name, entry);
    }

    @NotNull
    public static Element getElementByName(final String name) {
        final Element element = ElementRegistry.nameToElement.get(name);
        return element != null ? element : ElementRegistry.emptyElement;
    }

    @NotNull
    public static Element getElementByID(final byte id) {
        final String name = ElementRegistry.idToName.get((int) id);
        return ElementRegistry.getElementByName(name);
    }
}
