package models;

import core.Faces;
import core.GlobalVariables;
import toolbox.Color;
import toolbox.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class MeshModel {
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

    public void createCube(final Rectangle rect, final Color color) {
        for (int i = 0; i < 6; i++) {
            final float[] pureVertices = pureVerticesByFace[i];

            final float w = rect.getW() / 2;
            final float h = rect.getH() / 2;
            final float d = rect.getD() / 2;
            for (int j = 0; j < pureVertices.length; ) {
                verticesList.add(rect.getX() + pureVertices[j++] * 2 * w);
                verticesList.add(rect.getY() + pureVertices[j++] * 2 * h);
                verticesList.add(rect.getZ() + pureVertices[j++] * 2 * d);
            }

            final int startIDX = verticesList.size() / 3 - 4;
            indicesList.add(startIDX);
            indicesList.add(startIDX + 1);
            indicesList.add(startIDX + 3);
            indicesList.add(startIDX + 3);
            indicesList.add(startIDX + 1);
            indicesList.add(startIDX + 2);

            for (int j = 0; j < 4; j++) {
                final float density = densitiesByFace[i] * 127;
                colorList.add((byte) (density * color.getR()));
                colorList.add((byte) (density * color.getG()));
                colorList.add((byte) (density * color.getB()));
                colorList.add((byte) (density * color.getA()));
            }
        }
    }

    public void addFaceToMesh(final int x, final int y, final int z, final Faces face, final Color color) {
        final int idx = Faces.faceToIndex(face);
        final float[] pureVertices = pureVerticesByFace[idx];
        for (int j = 0; j < pureVertices.length; ) {
            verticesList.add(pureVertices[j++] + x + 0.5f);
            verticesList.add(pureVertices[j++] + y + 0.5f);
            verticesList.add(pureVertices[j++] + z + 0.5f);
        }

        final int startIDX = verticesList.size() / 3 - 4;
        indicesList.add(startIDX);
        indicesList.add(startIDX + 1);
        indicesList.add(startIDX + 3);
        indicesList.add(startIDX + 3);
        indicesList.add(startIDX + 1);
        indicesList.add(startIDX + 2);

        for (int j = 0; j < 4; j++) {
            final float density = densitiesByFace[idx] * 127;
            colorList.add((byte) (density * color.getR()));
            colorList.add((byte) (density * color.getG()));
            colorList.add((byte) (density * color.getB()));
            colorList.add((byte) (density * color.getA()));
        }
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

    public List<Float> getVerticesList() {
        return verticesList;
    }

    public List<Integer> getIndicesList() {
        return indicesList;
    }

    public List<Byte> getColorList() {
        return colorList;
    }
}
