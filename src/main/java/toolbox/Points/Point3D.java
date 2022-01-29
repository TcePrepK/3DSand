package toolbox.Points;

import org.joml.Vector3f;
import toolbox.Vector3D;

public class Point3D {
    public int x, y, z;

    public Point3D() {
        x = y = z = 0;
    }

    public Point3D(final int v) {
        x = y = z = v;
    }

    public Point3D(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(final float x, final float y, final float z) {
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
        this.z = (int) Math.floor(z);
    }

    public Point3D(final Vector3f pos) {
        x = (int) pos.x();
        y = (int) pos.y();
        z = (int) pos.z();
    }

    public Point3D add(final Point3D v) {
        return new Point3D(x + v.x, y + v.y, z + v.z);
    }

    public Point3D add(final int x, final int y, final int z) {
        return new Point3D(this.x + x, this.y + y, this.z + z);
    }

    public Point3D sub(final Point3D v) {
        return new Point3D(x - v.x, y - v.y, z - v.z);
    }

    public Point3D mult(final int v) {
        return new Point3D(x * v, y * v, z * v);
    }

    public Point3D div(final int v) {
        return new Point3D(x / v, y / v, z / v);
    }

    public Point3D abs() {
        return new Point3D(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    public String toString() {
        return "(X:" + x + " Y:" + y + " Z:" + z + ")";
    }

    public Vector3D toVector3D() {
        return new Vector3D(x, y, z);
    }
}
