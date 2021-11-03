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
    private int randVector2D;
    private int colorWeights;
    private int oldColorAttachment;
    private int oldDepthAttachment;

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
        randVector2D = super.getUniformLocation("randVector2D");
        colorWeights = super.getUniformLocation("colorWeights");

        oldColorAttachment = super.getUniformLocation("oldColorAttachment");
        oldDepthAttachment = super.getUniformLocation("oldDepthAttachment");
    }

    public void loadVariables() {
        ShaderProgram.load2DVector(resolution, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
        ShaderProgram.loadMatrix(viewMatrix, camera.getViewMatrix());
        ShaderProgram.load3DVector(chunkScale, new Vector3D(mapChunkSize));

        ShaderProgram.loadInt(oldColorAttachment, 1);
        ShaderProgram.loadInt(oldDepthAttachment, 2);
    }

    public void loadCameraVariables() {
        ShaderProgram.load3DVector(topLeftCorner, camera.getTopLeftCorner());
        ShaderProgram.load3DVector(xIncrement, camera.getxIncrement());
        ShaderProgram.load3DVector(yIncrement, camera.getyIncrement());
        ShaderProgram.load3DVector(cameraPos, camera.getPosition());
        ShaderProgram.load3DVector(textureScale, world.getBufferScale().toVector3D());
    }

    public void loadRandomVector() {
        ShaderProgram.load2DVector(randVector2D, new Vector2f(rand.nextFloat(), rand.nextFloat()));
    }

    public void loadColorWeights(final float frameCount) {
        ShaderProgram.load2DVector(colorWeights, new Vector2f(frameCount / (frameCount + 1), 1 / (frameCount + 1)));
    }
}
