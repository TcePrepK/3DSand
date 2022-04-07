package toolbox;

import org.joml.Vector3f;

public class Cube {
    private float x, y, z;
    private float w, h, d;

    public Cube() {
        this(0, 0, 0, 0, 0, 0);
    }

    public Cube(final float x, final float y, final float z, final float w, final float h, final float d) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.h = h;
        this.d = d;
    }

    public Cube setPosition(final float x, final float y, final float z) {
        return new Cube(x, y, z, w, h, d);
    }

    public Cube extend(final float s) {
        return new Cube(x - s, y - s, z - s, w + s * 2, h + s * 2, d + s * 2);
    }

    public Cube setSize(final float w, final float h, final float d) {
        return new Cube(x, y, z, w, h, d);
    }

    public Cube moveAdd(final Vector3f point) {
        return new Cube(x + point.x(), y + point.y(), z + point.z(), w, h, d);
    }

    public Cube moveAdd(final float x, final float y, final float z) {
        return new Cube(this.x + x, this.y + y, this.z + z, w, h, d);
    }

    public Cube sub(final float v) {
        return new Cube(x - v, y - v, z - v, w, h, d);
    }

    public Cube sub(final float a, final float b, final float c) {
        return new Cube(x - a, y - b, z - c, w, h, d);
    }

    public Cube mult(final float v) {
        return new Cube(x * v, y * v, z * v, w * v, h * v, d * v);
    }

    public Cube div(final float v) {
        return new Cube(x / v, y / v, z / v, w / v, h / v, d / v);
    }

    public Cube ceil() {
        final float nextX = (float) Math.floor(x);
        final float nextY = (float) Math.floor(y);
        final float nextZ = (float) Math.floor(z);
        final float nextW = (float) (Math.ceil(x + w) - nextX);
        final float nextH = (float) (Math.ceil(y + h) - nextY);
        final float nextD = (float) (Math.ceil(z + d) - nextZ);

        return new Cube(nextX, nextY, nextZ, nextW, nextH, nextD);
    }

    public Cube clamp(final Cube other) {
        final float nextX = Math.max(x, other.x);
        final float nextY = Math.max(y, other.y);
        final float nextZ = Math.max(z, other.z);
        final float nextW = Math.min(x + w, other.x + other.w) - nextX;
        final float nextH = Math.min(y + h, other.y + other.h) - nextY;
        final float nextD = Math.min(z + d, other.z + other.d) - nextZ;

        return new Cube(nextX, nextY, nextZ, nextW, nextH, nextD);
    }

    public Cube includeCube(final Cube other) {
        final float nextX = Math.min(x, other.x);
        final float nextY = Math.min(y, other.y);
        final float nextZ = Math.min(z, other.z);
        final float nextW = Math.max(x + w, other.x + other.w) - nextX;
        final float nextH = Math.max(y + h, other.y + other.h) - nextY;
        final float nextD = Math.max(z + d, other.z + other.d) - nextZ;

        return new Cube(nextX, nextY, nextZ, nextW, nextH, nextD);
    }

    public void includePoint(final float x, final float y, final float z) {
        if (x < this.x) {
            w += this.x - x;
            this.x = x;
        } else if (x > this.x + w) {
            w = x - this.x;
        }
        if (y < this.y) {
            h += this.y - h;
            this.y = h;
        } else if (y > this.y + h) {
            h = y - this.y;
        }
        if (z < this.z) {
            d += this.z - z;
            this.z = z;
        } else if (z > this.z + d) {
            d = z - this.z;
        }
    }

    @Override
    public String toString() {
        return "(X:" + x + " Y:" + y + " Z:" + z + " W:" + w + " H:" + h + " D:" + d + ")";
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    public float getH() {
        return h;
    }

    public float getD() {
        return d;
    }

    public void setW(final float w) {
        this.w = w;
    }

    public void setH(final float h) {
        this.h = h;
    }

    public void setD(final float d) {
        this.d = d;
    }
}
