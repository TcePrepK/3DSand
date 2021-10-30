package models;

import core.GlobalVariables;
import toolbox.Color;
import toolbox.Rectangle;

public class PlaneModelCreator {
    final static float[] pureVertices = {
            -1, 0, -1,
            1, 0, -1,
            -1, 0, 1,
            1, 0, 1,
    };

    final static float[] colorDensities = {
            0.85f,
            0.85f,
            0.85f,
            0.85f
    };

    final static int[] indices = {
            0, 1, 2,
            2, 1, 3
    };

    public static float[] getVertices(final float w, final float d) {
        final int size = PlaneModelCreator.pureVertices.length;
        final float[] vertices = new float[size];
        for (int i = 0; i < size; i += 0) {
            final float pureX = PlaneModelCreator.pureVertices[i];
            final float pureZ = PlaneModelCreator.pureVertices[i + 2];
            vertices[i++] = pureX * w / 2;
            vertices[i++] = 0;
            vertices[i++] = pureZ * d / 2;
        }
        return vertices;
    }

    public static RawModel getPlaneRawModel(final float w, final float d, final Color color) {
        final int size = PlaneModelCreator.colorDensities.length;
        final byte[] colors = new byte[size * 4];
        int i = 0;
        for (final float density : PlaneModelCreator.colorDensities) {
            colors[i++] = (byte) (density * color.getR() * 127);
            colors[i++] = (byte) (density * color.getG() * 127);
            colors[i++] = (byte) (density * color.getB() * 127);
            colors[i++] = (byte) (color.getA() * 127);
        }

        return GlobalVariables.loader.loadToVAO(PlaneModelCreator.getVertices(w, d), colors, PlaneModelCreator.indices);
    }

    public static RawModel getPlaneRawModel(final Rectangle rect, final Color color) {
        return PlaneModelCreator.getPlaneRawModel(rect.getW(), rect.getD(), color);
    }
}
