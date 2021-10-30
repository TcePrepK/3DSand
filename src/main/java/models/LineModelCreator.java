package models;

import core.GlobalVariables;
import toolbox.Color;

import java.util.ArrayList;
import java.util.List;

public class LineModelCreator {
    final float[][] pureVerticesByFace = {
            {
                    -0.5f, 0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
            },
            {
                    -0.5f, 0.5f, 0.5f,
                    -0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f
            },
            {
                    0.5f, 0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f
            },
            {
                    -0.5f, 0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, -0.5f, 0.5f,
                    -0.5f, 0.5f, 0.5f
            },
            {
                    -0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, 0.5f
            },
            {
                    -0.5f, -0.5f, 0.5f,
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, 0.5f
            }
    };

    final float[] densitiesByFace = {
            0.8f,
            1,
            0.85f,
            0.85f,
            0.95f,
            0.95f
    };

    private final List<Float> verticesList = new ArrayList<>();
    private final List<Integer> indicesList = new ArrayList<>();
    private final List<Byte> colorList = new ArrayList<>();

    public void addPoint(final float x, final float y, final float z, final Color color) {
        verticesList.add(x);
        verticesList.add(y);
        verticesList.add(z);

        indicesList.add(verticesList.size() / 3 - 1);

        colorList.add((byte) (127 * color.getR()));
        colorList.add((byte) (127 * color.getG()));
        colorList.add((byte) (127 * color.getB()));
        colorList.add((byte) (127 * color.getA()));
    }

    public RawModel createModelWithMesh() {
        final float[] vertices = new float[verticesList.size()];
        final int[] indices = new int[indicesList.size()];
        final byte[] colors = new byte[colorList.size()];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = verticesList.get(i);
        }

        for (int i = 0; i < indices.length; i++) {
            indices[i] = indicesList.get(i);
        }

        for (int i = 0; i < colors.length; i++) {
            colors[i] = colorList.get(i);
        }

        return GlobalVariables.loader.loadToVAO(vertices, colors, indices);
    }
}
