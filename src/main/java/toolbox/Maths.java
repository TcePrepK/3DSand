package toolbox;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Maths {
    public static Vector3D max(final Vector3D a, final Vector3D b) {
        return new Vector3D(
                Math.max(a.x, b.x),
                Math.max(a.y, b.y),
                Math.max(a.z, b.z)
        );
    }

    public static Vector3D max(final Vector3D a, final float v) {
        return new Vector3D(
                Math.max(a.x, v),
                Math.max(a.y, v),
                Math.max(a.z, v)
        );
    }

    public static float barryCentric(final Vector3f p1, final Vector3f p2, final Vector3f p3, final Vector2f pos) {
        final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        final float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        final float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        final float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Vector2f rotateVector2D(final Vector2f vec, final float angle) {
        final double theta = Math.toRadians(angle);
        final float sinTheta = (float) Math.sin(theta);
        final float cosTheta = (float) Math.cos(theta);
        return new Vector2f(vec.x * cosTheta - vec.y * sinTheta, vec.x * sinTheta + vec.y * cosTheta);
    }

    public static float angleBetween2DVectors(final Vector2f a, final Vector2f b) {
        final double angle = Math.atan2(a.x, a.y) - Math.atan2(b.x, b.y);
        return (float) Math.toDegrees(angle);
    }

    public static float clamp(final float num, final float min, final float max) {
        if (min > max) {
            return num;
        }

        if (num < min) {
            return min;
        }

        return Math.min(num, max);

    }

    public static float lerp(final float a, final float b, final float t) {
        return a + (b - a) * t;
    }

    public static Vector3D lerp(final Vector3D a, final Vector3D b, final float t) {
        return new Vector3D(
                Maths.lerp(a.x, b.x, t),
                Maths.lerp(a.y, b.y, t),
                Maths.lerp(a.z, b.z, t)
        );
    }
}
