package toolbox;

import org.joml.Vector3f;

public class Rectangle {
    private float x, y, z;
    private float w, h, d;

    public Rectangle() {
        this(0, 0, 0, 0, 0, 0);
    }

    public Rectangle(final float x, final float y, final float z, final float w, final float h, final float d) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.h = h;
        this.d = d;
    }

    public Rectangle setPosition(final float x, final float y, final float z) {
        return new Rectangle(x, y, z, w, h, d);
    }

    public Rectangle makeBigger(final float a) {
        return new Rectangle(x, y, z, w + a, h + a, d + a);
    }

    public Rectangle setSize(final float w, final float h, final float d) {
        return new Rectangle(x, y, z, w, h, d);
    }

    public Rectangle moveAdd(final Vector3f point) {
        return new Rectangle(x + point.x(), y + point.y(), z + point.z(), w, h, d);
    }

    public Rectangle moveAdd(final float x, final float y, final float z) {
        return new Rectangle(this.x + x, this.y + y, this.z + z, w, h, d);
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
