package elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FillerRegistry {
    private final HashMap<String, List<String>> fillerToElements = new HashMap<>();

    public void addFiller(final String filler, final String element) {
        if (!fillerToElements.containsKey(filler)) {
            fillerToElements.put(filler, new ArrayList<>());
        }

        if (fillerToElements.get(filler).contains(element)) {
            return;
        }

        fillerToElements.get(filler).add(element);
    }

    public List<String> getFiller(final String filler) {
        if (!fillerToElements.containsKey(filler)) {
            fillerToElements.put(filler, new ArrayList<>());
        }

        return fillerToElements.get(filler);
    }
}
