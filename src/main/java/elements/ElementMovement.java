package elements;

import toolbox.Points.Point3D;

import java.util.ArrayList;
import java.util.List;

public class ElementMovement {
    private final List<Point3D> positions = new ArrayList<>();
    private final List<List<String>> inputsId = new ArrayList<>();
    private final List<String> outputsId = new ArrayList<>();
    private final float probability;

    public ElementMovement(final float probability) {
        this.probability = probability;
    }

    public ElementMovement() {
        probability = 1;
    }

    public void addMovement(final Point3D pos, final List<String> inputId, final String outputId) {
        positions.add(pos);

        inputsId.add(inputId);
        outputsId.add(outputId);
    }

    public List<Point3D> getPositions() {
        return positions;
    }

    public List<List<String>> getInputsId() {
        return inputsId;
    }

    public List<String> getOutputsId() {
        return outputsId;
    }

    public float getProbability() {
        return probability;
    }
}
