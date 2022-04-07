package elements;

import game.Chunk;
import toolbox.Points.Point3D;

import java.util.List;

public class Movement {
    private final Point3D offset;
    private final List<String> inputs;
    private final String output;

    public Movement(final FillerRegistry registry, final Point3D offset, final String inputFiller, final String output) {
        this.offset = offset;
        this.output = output;

        inputs = registry.getFiller(inputFiller);
    }

    public boolean checkInput(final Chunk chunk, final Point3D pos) {
        final Element element = chunk.getElement(pos);
        for (final String input : inputs) {
            if (input == null && element.getName() == null) {
                return true;
            }

            if (input != null && input.equals(element.getName())) {
                return true;
            }
        }

        return false;
    }

    public void setOutput(final Chunk chunk, final Point3D pos) {
        chunk.setElement(pos, ElementRegistry.getElementByName(output));
    }

    public Point3D getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "Movement{" +
                "offset=" + offset +
                ", inputs=" + inputs +
                ", output=" + output +
                '}';
    }
}
