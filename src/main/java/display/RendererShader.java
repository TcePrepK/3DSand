package display;

import core.ShaderProgram;
import org.joml.Vector2f;
import toolbox.Vector3D;

import static core.GlobalVariables.*;

public class RendererShader extends ShaderProgram {
    private static final String VERTEX_FILE = "/shaders/vertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/fragmentShader.glsl";

    private int viewMatrix;
    private int topLeftCorner;
    private int xIncrement;
    private int yIncrement;
    private int resolution;
    private int cameraPos;
    private int textureScale;
    private int chunkScale;
    private int worldTexture;
    private int rVector2D;
    private int colorWeights;

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
        resolution = super.getUniformLocation("resolution");
        cameraPos = super.getUniformLocation("cameraPos");
        textureScale = super.getUniformLocation("textureScale");
        chunkScale = super.getUniformLocation("chunkScale");
        worldTexture = super.getUniformLocation("worldTexture");
        rVector2D = super.getUniformLocation("rVector2D");
        colorWeights = super.getUniformLocation("colorWeights");
    }

    public void loadVariables() {
        ShaderProgram.load2DVector(resolution, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
        ShaderProgram.loadMatrix(viewMatrix, camera.getViewMatrix());
        ShaderProgram.load3DVector(chunkScale, new Vector3D(mapChunkSize));
        ShaderProgram.loadInt(worldTexture, 1);
    }

    public void loadCameraVariables() {
        ShaderProgram.load3DVector(topLeftCorner, camera.getTopLeftCorner());
        ShaderProgram.load3DVector(xIncrement, camera.getxIncrement());
        ShaderProgram.load3DVector(yIncrement, camera.getyIncrement());
        ShaderProgram.load3DVector(cameraPos, camera.getPosition());
        ShaderProgram.load3DVector(textureScale, world.getBufferScale().toVector3D());
    }

    public void loadRandomVector() {
        ShaderProgram.load2DVector(rVector2D, new Vector2f(rand.nextFloat(), rand.nextFloat()));
    }

    public void loadColorWeights(final float frameCount) {
        ShaderProgram.load2DVector(colorWeights, new Vector2f(frameCount / (frameCount + 1), 1 / (frameCount + 1)));
    }
}
