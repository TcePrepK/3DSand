package models;

import org.joml.Vector3f;

public class ColoredModel {
    private final RawModel rawModel;
    private final Vector3f position;

    public ColoredModel(final float x, final float y, final float z, final RawModel model) {
        rawModel = model;
        position = new Vector3f(x, y, z);
    }

    public void movePosition(final float x, final float y, final float z) {
        position.x += x;
        position.y += y;
        position.z += z;
    }

    public void setPosition(final Vector3f position) {
        setPosition(position.x(), position.y(), position.z());
    }

    public void setPosition(final float x, final float y, final float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public Vector3f getPosition() {
        return position;
    }
}
