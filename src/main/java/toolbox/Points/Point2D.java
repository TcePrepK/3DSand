package toolbox.Points;

import org.joml.Vector2f;

public class Point2D {
    public int x, y;

    public Point2D() {
        x = y = 0;
    }

    public Point2D(final int v) {
        x = y = v;
    }

    public Point2D(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(final float x, final float y) {
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
    }

    public Point2D(final Vector2f pos) {
        x = (int) pos.x();
        y = (int) pos.y();
    }

    public Point2D add(final Point2D v) {
        return new Point2D(x + v.x, y + v.y);
    }

    public Point2D add(final int x, final int y) {
        return new Point2D(this.x + x, this.y + y);
    }

    public Point2D sub(final Point2D v) {
        return new Point2D(x - v.x, y - v.y);
    }

    public Point2D mult(final int v) {
        return new Point2D(x * v, y * v);
    }

    public Point2D div(final int v) {
        return new Point2D(x / v, y / v);
    }

    public Point2D abs() {
        return new Point2D(Math.abs(x), Math.abs(y));
    }

    @Override
    public String toString() {
        return "(X:" + x + " Y:" + y + ")";
    }
}
