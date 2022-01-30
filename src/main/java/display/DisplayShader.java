package display;

import core.ShaderProgram;

public class DisplayShader extends ShaderProgram {
    private static final String VERTEX_FILE = "/shaders/displayVertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/displayFragmentShader.glsl";

    public DisplayShader() {
        super(DisplayShader.VERTEX_FILE, DisplayShader.FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {

    }
}
