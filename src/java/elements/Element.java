package elements;

import core.GlobalVariables;
import game.Chunk;
import game.World;
import toolbox.Color;
import toolbox.Keyboard;
import toolbox.Points.Point3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static elements.ElementRegistry.getElementByName;
import static elements.ElementRegistry.lastId;

public abstract class Element {
    private final HashMap<String, List<String>> elementNameRegistery = new HashMap<>();
    private final List<ElementMovement> movementRegistery = new ArrayList<>();

    private String name = null;
    private int id = 0;
    private Color color;
    private boolean transparent;

    private final String ANYID = "any_id";

    private final int NEGATIVE = -1;
    private final int POSITIVE = 1;
    private final int BOTH = 0;

    protected void NAME(final String name) {
        this.name = name;
        id = lastId++;

        registerElement("T", name);
        registerElement("*", ANYID);
        registerElement("_", null);
    }

    protected void KEY(final String keys) {
        if (name == null) {
            System.err.println("You must define name of the element first!");
        }

        Keyboard.keyPressed.add(() -> {
            GlobalVariables.currentMat = name;
        }, keys);
    }

    protected void COLOR(final Color color) {
        if (name == null) {
            System.err.println("You must define name of the element first!");
        }

        this.color = color;
        transparent = color.getA() != 1;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public boolean isTransparent() {
        return transparent;
    }

    // Moves
    public boolean update(final Point3D startingPos) {
        if (movementRegistery.size() == 0) {
            return false;
        }

        boolean outOfLuck = false;
        final List<ElementMovement> clonedMovements = new ArrayList<>(movementRegistery);
        while (clonedMovements.size() > 0) {
            final int idx = GlobalVariables.rand.nextInt(clonedMovements.size());
            final ElementMovement movement = clonedMovements.get(idx);
            clonedMovements.remove(idx);

            final List<Point3D> positions = movement.getPositions();
            if (positions.size() == 0) {
                continue;
            }

            final List<List<String>> inputsId = movement.getInputsId();
            final List<String> outputsId = movement.getOutputsId();
            boolean possiblePosition = true;
            for (int i = 0; i < positions.size(); i++) {
                final Point3D pos = startingPos.add(positions.get(i));

                final Chunk chunk = World.getChunkAtTile(pos, true);
                if (chunk == null) {
                    possiblePosition = false;
                    break;
                }

                final List<String> inputIds = inputsId.get(i);
                final Element element = chunk.getElement(pos);
                final String elementId = element == null ? null : element.getName();

                boolean possibleInput = false;
                for (int j = 1; j < inputIds.size(); j++) {
                    final String inputId = inputIds.get(j);
                    if (inputId != null && inputId.equals(ANYID)) {
                        possibleInput = true;
                        break;
                    }

                    if (inputId == null && elementId == null) {
                        possibleInput = true;
                        break;
                    }

                    if (inputId != null && inputId.equals(elementId)) {
                        possibleInput = true;
                        break;
                    }
                }

                if (!possibleInput) {
                    possiblePosition = false;
                    break;
                }
            }

            if (!possiblePosition) {
                continue;
            }

            if (GlobalVariables.rand.nextFloat() > movement.getProbability()) {
                outOfLuck = true;
                continue;
            }

            boolean changedAnything = false;
            for (int i = 0; i < positions.size(); i++) {
                final Point3D pos = startingPos.add(positions.get(i));

                final Chunk chunk = World.getChunkAtTile(pos, false);
                final String outputId = outputsId.get(i);

                if (outputId != null && outputId.equals(ANYID)) {
                    continue;
                }

                changedAnything = true;
                final Element oldElement = chunk.getElement(pos);
                if (outputId == null) {
                    if (oldElement != null) {
                        chunk.setElement(pos, null);
                    }
                } else {
                    if (oldElement != null && outputId.equals(oldElement.getName())) {
                        continue;
                    }
                    chunk.setElement(pos, getElementByName(outputId));
                    chunk.awakeGrid(pos);
                }
            }

            if (changedAnything) {
                return true;
            }
        }

        if (outOfLuck) {
            final Chunk chunk = World.getChunkAtTile(startingPos, false);
            chunk.awakeGrid(startingPos);
            return true;
        }

        return false;
    }

    protected void registerElement(final String code, final String name) {
        registerElement(code, name, name);
    }

    protected void registerElement(final String name, final String output, final String... inputs) {
        if (inputs == null) {
            return;
        }

        final List<String> idList = new ArrayList<>();
        idList.add(output);

        for (final String id : inputs) {
            if (id != null && id.equals(ANYID)) {
                idList.clear();
                idList.add(ANYID);
                idList.add(ANYID);
                break;
            }

            idList.add(id);
        }

        if (elementNameRegistery.replace(name, idList) == null) {
            elementNameRegistery.put(name, idList);
        }
    }

    private void registerMove(final Point dir, final String a, final String b, final boolean invert, final float probability) {
        if (a.startsWith("/") || b.startsWith("/") || a.contains("\n") || b.contains("\n") || a.length() != b.length() || a.equals(b)) {
            return;
        }

        final String[] inputLayers = a.split("\\/");
        final String[] outputLayers = b.split("\\/");

        if (inputLayers.length != outputLayers.length) {
            return;
        }

        int offY0 = 0;
        while (inputLayers[0].startsWith("^")) {
            offY0++;

            inputLayers[0] = inputLayers[0].substring(1);
        }

        int offY1 = 0;
        while (outputLayers[0].startsWith("^")) {
            offY1++;

            outputLayers[0] = outputLayers[0].substring(1);
        }

        if (offY0 != offY1) {
            return;
        }

        final ElementMovement movement = new ElementMovement(probability);
        ElementMovement invertedMovement = null;
        if (invert) {
            invertedMovement = new ElementMovement(probability);
        }
        for (int i = 0; i < inputLayers.length; i++) {
            final String[] inputLayer = inputLayers[i].split("(?!^)");
            final String[] outputLayer = outputLayers[i].split("(?!^)");
            for (int j = 0; j < inputLayer.length; j++) {
                final String inputName = inputLayer[j];
                final String outputName = outputLayer[j];

                final List<String> inputId = elementNameRegistery.get(inputName);
                final String outputId = elementNameRegistery.get(outputName).get(0);
                final Point3D pos = new Point3D(dir.x * j, -i + offY0, dir.y * j);
                movement.addMovement(pos, inputId, outputId);

                if (invert) {
                    invertedMovement.addMovement(new Point3D(dir.x * -j, -i + offY0, dir.y * -j), inputId, outputId);
                }
            }
        }

        movementRegistery.add(movement);

        if (invert && invertedMovement.getPositions().size() != 0) {
            movementRegistery.add(invertedMovement);
        }
    }

    protected void X(final String a, final String b, final int dir, final float probability) {
        if (Math.abs(dir) > 1) {
            System.err.println("Registered dir is wrong: " + dir);
            return;
        }

        if (dir == 0) {
            registerMove(new Point(1, 0), a, b, true, probability);
            return;
        }

        registerMove(new Point(dir, 0), a, b, false, probability);
    }

    protected void Z(final String a, final String b, final int dir, final float probability) {
        if (Math.abs(dir) > 1) {
            System.err.println("Registered dir is wrong: " + dir);
            return;
        }

        if (dir == 0) {
            registerMove(new Point(0, 1), a, b, true, probability);
            return;
        }

        registerMove(new Point(0, dir), a, b, false, probability);
    }

    protected void XZ(final String a, final String b, final int dir, final float probability) {
        if (Math.abs(dir) > 1) {
            System.err.println("Registered dir is wrong: " + dir);
            return;
        }

        if (dir == 0) {
            registerMove(new Point(1, 1), a, b, true, probability);
            return;
        }

        registerMove(new Point(dir, dir), a, b, false, probability);
    }

    protected void GRAVITY() {
        registerMove(new Point(0, 0), "T/_", "_/T", false, 1);
    }

    protected void SAND() {
        registerElement("_", null, null, "Water");

        // Air

        X("T/_", "_/T", POSITIVE, 1);

        X("T_/T_", "__/TT", BOTH, 0.25f);
        Z("T_/T_", "__/TT", BOTH, 0.25f);

        X("T_/T_", "__/TT", BOTH, 0.25f);
        Z("T_/T_", "__/TT", BOTH, 0.25f);
    }

    protected void WATER() {
        GRAVITY();

        final float probability = 0.1f;
        X("T_/*_", "__/*T", BOTH, probability);
        Z("T_/*_", "__/*T", BOTH, probability);
        XZ("T_/*_", "__/*T", BOTH, probability);

        X("T_/**", "_T/**", BOTH, probability);
        Z("T_/**", "_T/**", BOTH, probability);
        XZ("T_/**", "_T/**", BOTH, probability);
    }
}
