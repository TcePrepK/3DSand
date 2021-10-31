package display;

import core.ShaderProgram;
import toolbox.Vector3D;

import static core.GlobalVariables.*;

public class RendererShader extends ShaderProgram {
    private static final String VERTEX_FILE = "/shaders/vertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/fragmentShader.glsl";

    private int viewMatrix;
    private int topLeftCorner;
    private int xIncrement;
    private int yIncrement;
    private int screenWidth;
    private int screenHeight;
    private int cameraPos;
    private int textureScale;
    private int chunkScale;

    public RendererShader() {
        super(RendererShader.VERTEX_FILE, RendererShader.FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        viewMatrix = super.getUniformLocation("mvpMatrix");
        topLeftCorner = super.getUniformLocation("topLeftCorner");
        xIncrement = super.getUniformLocation("xIncrement");
        yIncrement = super.getUniformLocation("yIncrement");
        screenWidth = super.getUniformLocation("screenWidth");
        screenHeight = super.getUniformLocation("screenHeight");
        cameraPos = super.getUniformLocation("cameraPos");
        textureScale = super.getUniformLocation("textureScale");
        chunkScale = super.getUniformLocation("chunkScale");
    }

    public void loadVariables() {
        ShaderProgram.loadInt(screenWidth, DisplayManager.WIDTH);
        ShaderProgram.loadInt(screenHeight, DisplayManager.HEIGHT);
        ShaderProgram.loadMatrix(viewMatrix, camera.getViewMatrix());
        ShaderProgram.load3DVector(chunkScale, new Vector3D(mapChunkSize));
    }

    public void loadCameraVariables() {
        ShaderProgram.load3DVector(topLeftCorner, camera.getTopLeftCorner());
        ShaderProgram.load3DVector(xIncrement, camera.getxIncrement());
        ShaderProgram.load3DVector(yIncrement, camera.getyIncrement());
        ShaderProgram.load3DVector(cameraPos, camera.getPosition());
        ShaderProgram.load3DVector(textureScale, world.getBufferScale().toVector3D());
    }
}
