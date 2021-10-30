package core;

import org.joml.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f colour;

    public Light(final Vector3f position, final Vector3f colour) {
        this.position = position;
        this.colour = colour;
    }


    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public void setColour(final Vector3f colour) {
        this.colour = colour;
    }
}
