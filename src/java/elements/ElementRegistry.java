package elements;

import elements.liquids.Water;
import elements.solids.movables.Dirt;
import elements.solids.movables.Sand;
import elements.weirds.Filler;

import java.util.HashMap;

public class ElementRegistry {
    private static final HashMap<String, Element> nameToElement = new HashMap<>();
    private static final HashMap<Integer, String> idToName = new HashMap<>();
    public static int lastId = 1;

    public void init() {
        register(new Sand());
        register(new Dirt());
        register(new Water());
        register(new Filler());
    }

    private void register(final Element entry) {
        final String name = entry.getName();

        if (ElementRegistry.nameToElement.containsKey(name)) {
            System.err.println("Tried to register same element more than once: " + name);
            return;
        }

        ElementRegistry.idToName.put(entry.getId(), name);
        ElementRegistry.nameToElement.put(name, entry);
    }

    public static Element getElementByName(final String name) {
        return ElementRegistry.nameToElement.get(name);
    }
}
