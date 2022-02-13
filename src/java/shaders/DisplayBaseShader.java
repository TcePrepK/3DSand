package shaders;

import display.DisplayManager;
import org.joml.Vector2f;

public class DisplayBaseShader extends BaseShader {
    private static final String VERTEX_FILE = "/shaders/displayVertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/displayFragmentShader.glsl";

    private int resolution;

    public DisplayBaseShader() {
        super(DisplayBaseShader.VERTEX_FILE, DisplayBaseShader.FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        resolution = super.getUniformLocation("resolution");
    }

    public void loadResolution() {
        BaseShader.load2DVector(resolution, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
    }
}
