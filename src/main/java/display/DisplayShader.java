package display;

import core.ShaderProgram;
import org.joml.Vector2f;

public class DisplayShader extends ShaderProgram {
    private static final String VERTEX_FILE = "/shaders/displayVertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/displayFragmentShader.glsl";

    private int resolution;

    public DisplayShader() {
        super(DisplayShader.VERTEX_FILE, DisplayShader.FRAGMENT_FILE);
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
        ShaderProgram.load2DVector(resolution, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
    }
}
