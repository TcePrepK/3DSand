package elements;

import core.GlobalVariables;
import core.Keyboard;
import toolbox.Color;
import toolbox.Points.Point3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static core.GlobalVariables.rand;
import static elements.ElementRegistry.lastId;

public abstract class Element {
    private final HashMap<String, List<String>> elementNameRegistry = new HashMap<>();
    private final List<ElementMovement> movementRegistry = new ArrayList<>();

    private final List<MovementSet> movementList = new ArrayList<>();
    private final FillerRegistry fillerRegistry = new FillerRegistry();

    private String name = null;
    private int id = 0;
    private Color color;
    private boolean transparent;

    private final int NEGATIVE = -1;
    private final int POSITIVE = 1;
    private final int BOTH = 0;

    protected void NAME(final String name) {
        this.name = name;
        id = lastId++;

        addFiller("T", name);
        addFiller("*", "Any");
        addFiller("_", null);
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

    public boolean update(final Point3D startingPos) {
        if (movementList.isEmpty()) {
            return false;
        }

        final List<MovementSet> clonedMovements = new ArrayList<>(movementList);
        while (!clonedMovements.isEmpty()) {
            final int randomIndex = rand.nextInt(clonedMovements.size());
            final MovementSet movementSet = clonedMovements.get(randomIndex);
            clonedMovements.remove(randomIndex);

            if (!movementSet.checkPosition(startingPos)) {
                continue;
            }

            movementSet.setPosition(startingPos);
            return true;
        }

        return false;
    }
//        boolean outOfLuck = false;
//        final List<ElementMovement> clonedMovements = new ArrayList<>(movementRegistry);
//        while (!clonedMovements.isEmpty()) {
//            final int idx = rand.nextInt(clonedMovements.size());
//            final ElementMovement movement = clonedMovements.get(idx);
//            clonedMovements.remove(idx);
//
//            final List<Point3D> positions = movement.getPositions();
//            if (positions.size() == 0) {
//                continue;
//            }
//
//            final List<List<String>> inputsId = movement.getInputsId();
//            final List<String> outputsId = movement.getOutputsId();
//            boolean possiblePosition = true;
//            for (int i = 0; i < positions.size(); i++) {
//                final Point3D pos = startingPos.add(positions.get(i));
//
//                final Chunk chunk = chunkManager.getChunkWorldSpace(pos.x, pos.y, pos.z, true);
//                if (chunk == null) {
//                    possiblePosition = false;
//                    break;
//                }
//
//                final List<String> inputIds = inputsId.get(i);
//                final Element element = chunk.getElement(pos);
//                final String elementId = element == null ? null : element.getName();
//
//                boolean possibleInput = false;
//                for (int j = 1; j < inputIds.size(); j++) {
//                    final String inputId = inputIds.get(j);
//                    if (inputId != null && inputId.equals(AnyID)) {
//                        possibleInput = true;
//                        break;
//                    }
//
//                    if (inputId == null && elementId == null) {
//                        possibleInput = true;
//                        break;
//                    }
//
//                    if (inputId != null && inputId.equals(elementId)) {
//                        possibleInput = true;
//                        break;
//                    }
//                }
//
//                if (!possibleInput) {
//                    possiblePosition = false;
//                    break;
//                }
//            }
//
//            if (!possiblePosition) {
//                continue;
//            }
//
//            if (GlobalVariables.rand.nextFloat() > movement.getProbability()) {
//                outOfLuck = true;
//                continue;
//            }
//
//            boolean changedAnything = false;
//            for (int i = 0; i < positions.size(); i++) {
//                final Point3D pos = startingPos.add(positions.get(i));
//
//                final Chunk chunk = chunkManager.getChunkWorldSpace(pos.x, pos.y, pos.z, false);
//                final String outputId = outputsId.get(i);
//
//                if (outputId != null && outputId.equals(AnyID)) {
//                    continue;
//                }
//
//                changedAnything = true;
//                final Element oldElement = chunk.getElement(pos);
//                if (outputId == null) {
//                    if (oldElement != null) {
//                        chunk.setElement(pos, null);
//                    }
//                } else {
//                    if (oldElement != null && outputId.equals(oldElement.getName())) {
//                        continue;
//                    }
//                    chunk.setElement(pos, getElementByName(outputId));
//                    chunk.awakeGrid(pos);
//                }
//            }
//
//            if (changedAnything) {
//                return true;
//            }
//        }
//
//        if (outOfLuck) {
//            final Chunk chunk = chunkManager.getChunkWorldSpace(startingPos.x, startingPos.y, startingPos.z, false);
//            chunk.awakeGrid(startingPos);
//            return true;
//        }
//
//        return false;
//    }
//
//    protected void registerElement(final String code, final String name) {
//        registerElement(code, name, name);
//    }
//
//    protected void registerElement(final String name, final String output, final String inputs) {
//        if (inputs == null) {
//            return;
//        }
//
//        final List<String> idList = new ArrayList<>();
//        idList.add(output);
//
//        for (final String id : inputs) {
//            if (id != null && id.equals(AnyID)) {
//                idList.clear();
//                idList.add(AnyID);
//                idList.add(AnyID);
//                break;
//            }
//
//            idList.add(id);
//        }
//
//        if (elementNameRegistry.replace(name, idList) == null) {
//            elementNameRegistry.put(name, idList);
//        }
//    }

    protected void addFiller(final String filler, final String element) {
        fillerRegistry.addFiller(filler, element);
    }

    protected List<String> getFiller(final String filler) {
        return fillerRegistry.getFiller(filler);
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

        final MovementSet movementSet = new MovementSet(fillerRegistry);
        final MovementSet invertedMovementSet = new MovementSet(fillerRegistry);
        for (int i = 0; i < inputLayers.length; i++) {
            final String[] inputLayer = inputLayers[i].split("(?!^)");
            final String[] outputLayer = outputLayers[i].split("(?!^)");
            for (int j = 0; j < inputLayer.length; j++) {
                final String input = inputLayer[j];
                final String output = getFiller(outputLayer[j]).get(0);

                movementSet.addMovement(new Point3D(dir.x * j, offY0 - i, dir.y * j), input, output);
                if (invert) {
                    invertedMovementSet.addMovement(new Point3D(dir.x * -j, offY0 - i, dir.y * -j), input, output);
                }
            }
        }

        if (!movementSet.isEmpty()) {
            movementList.add(movementSet);
        }

        if (!invertedMovementSet.isEmpty()) {
            movementList.add(invertedMovementSet);
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
//        addFiller("e", EmptyID);
//        addFiller("e", "Water");

        // Air

//        X("T/e", "_/T", POSITIVE, 1);
//
//        X("Te/Te", "__/TT", BOTH, 0.25f);
//        Z("Te/Te", "__/TT", BOTH, 0.25f);
//
//        X("Te/Te", "__/TT", BOTH, 0.25f);
//        Z("Te/Te", "__/TT", BOTH, 0.25f);
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

    public List<MovementSet> getMovementList() {
        return movementList;
    }
}
