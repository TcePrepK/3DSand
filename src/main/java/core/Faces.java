package core;

import org.joml.Vector3f;

public enum Faces {
    BACK,
    FRONT,
    RIGHT,
    LEFT,
    TOP,
    BOTTOM;

    public static Vector3f[] allFaceVectors = new Vector3f[]{
            Faces.getFaceVector(Faces.BACK),
            Faces.getFaceVector(Faces.FRONT),
            Faces.getFaceVector(Faces.RIGHT),
            Faces.getFaceVector(Faces.LEFT),
            Faces.getFaceVector(Faces.TOP),
            Faces.getFaceVector(Faces.BOTTOM)
    };

    static Vector3f getFaceVector(final Faces face) {
        switch (face) {
            case BACK:
                return new Vector3f(0, 0, -1);
            case FRONT:
                return new Vector3f(0, 0, 1);
            case RIGHT:
                return new Vector3f(1, 0, 0);
            case LEFT:
                return new Vector3f(-1, 0, 0);
            case TOP:
                return new Vector3f(0, 1, 0);
            case BOTTOM:
                return new Vector3f(0, -1, 0);
            default:
                return new Vector3f();
        }
    }

    static Faces reverseFace(final Faces face) {
        if (face == null) {
            return null;
        }

        switch (face) {
            case BACK:
                return Faces.FRONT;
            case FRONT:
                return Faces.BACK;
            case RIGHT:
                return Faces.LEFT;
            case LEFT:
                return Faces.RIGHT;
            case TOP:
                return Faces.BOTTOM;
            case BOTTOM:
                return Faces.TOP;
            default:
                return null;
        }
    }

    public static Faces faceVectorToFace(final Vector3f vector) {
        if (Faces.getFaceVector(Faces.BACK).equals(vector)) {
            return Faces.BACK;
        } else if (Faces.getFaceVector(Faces.FRONT).equals(vector)) {
            return Faces.FRONT;
        } else if (Faces.getFaceVector(Faces.RIGHT).equals(vector)) {
            return Faces.RIGHT;
        } else if (Faces.getFaceVector(Faces.LEFT).equals(vector)) {
            return Faces.LEFT;
        } else if (Faces.getFaceVector(Faces.TOP).equals(vector)) {
            return Faces.TOP;
        } else if (Faces.getFaceVector(Faces.BOTTOM).equals(vector)) {
            return Faces.BOTTOM;
        }

        return null;
    }

    public static int faceToIndex(final Faces face) {
        if (face == null) {
            return -1;
        }

        switch (face) {
            case BACK:
                return 0;
            case FRONT:
                return 1;
            case RIGHT:
                return 2;
            case LEFT:
                return 3;
            case TOP:
                return 4;
            case BOTTOM:
                return 5;
            default:
                return -1;
        }
    }
}
