package toolbox;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import toolbox.Points.Point3D;

public class Vector3D {
    public float x, y, z;

    public Vector3D() {
        x = y = z = 0;
    }

    public Vector3D(final float v) {
        x = y = z = v;
    }

    public Vector3D(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        return div(length());
    }

    public float dot(final Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3D cross(final Vector3D v) {
        return new Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public Vector3D floor() {
        return new Vector3D((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Vector3D mod(final int v) {
        return new Vector3D(x % v, y % v, z % v);
    }

    public Vector3D add(final Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    public Vector3D add(final float v) {
        return new Vector3D(x + v, y + v, z + v);
    }

    public Vector3D add(final float x, final float y, final float z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D sub(final Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    public Vector3D sub(final float v) {
        return new Vector3D(x - v, y - v, z - v);
    }

    public Vector3D sub(final float x, final float y, final float z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D mult(final float v) {
        return new Vector3D(x * v, y * v, z * v);
    }

    public Vector3D mult(final Vector3D v) {
        return new Vector3D(x * v.x, y * v.y, z * v.z);
    }

    public Vector3D mult(final Matrix4f matrix, final float w) {
        final Vector3D result = new Vector3D();

        result.x += matrix.m00() * x + matrix.m01() * y + matrix.m02() * z + matrix.m03() * w;
        result.y += matrix.m10() * x + matrix.m11() * y + matrix.m12() * z + matrix.m13() * w;
        result.z += matrix.m20() * x + matrix.m21() * y + matrix.m22() * z + matrix.m23() * w;

        return result;
    }

    public Vector3D div(final float v) {
        return new Vector3D(x / v, y / v, z / v);
    }

    public Vector3D div(final Vector3D v) {
        return new Vector3D(x / v.x, y / v.y, z / v.z);
    }

    @Override
    public String toString() {
        return "(X:" + x + " Y:" + y + " Z:" + z + ")";
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }

    public Point3D toPoint3D() {
        return new Point3D(x, y, z);
    }

    public Color toColor() {
        return new Color(x, y, z);
    }
}
