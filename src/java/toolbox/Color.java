package toolbox;

public class Color {
    private final float r, g, b, a;

    public Color() {
        r = g = b = a = 0;
    }

    public Color(final float r, final float g, final float b, final float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(final float r, final float g, final float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        a = 1;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }
}
