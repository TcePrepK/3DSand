package display;

import core.DisplayManager;
import core.GlobalVariables;
import core.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import toolbox.Vector3D;

import static core.GlobalVariables.*;

public class RendererShader extends ShaderProgram {
    private static final String VERTEX_FILE = "/shaders/mainVertexShader.glsl";
    private static final String FRAGMENT_FILE = "/shaders/mainFragmentShader.glsl";

    private int viewMatrix;
    private int oldMVPMatrix;
    private int oldViewMatrix;
    private int displayRes;
    private int viewportRes;
    private int lookFrom;
    private int lookTo;

    private int textureScale;
    private int chunkScale;
    private int randVector2D;
    private int wFactor;
    private int resetEverything;
    private int oldCameraPos;
    private int renderingFractal;

    public RendererShader() {
        super(RendererShader.VERTEX_FILE, RendererShader.FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        viewMatrix = super.getUniformLocation("viewMatrix");
        oldMVPMatrix = super.getUniformLocation("oldMVPMatrix");
        oldViewMatrix = super.getUniformLocation("oldViewMatrix");
        displayRes = super.getUniformLocation("displayRes");
        viewportRes = super.getUniformLocation("viewportRes");
        lookFrom = super.getUniformLocation("lookFrom");
        lookTo = super.getUniformLocation("lookTo");

        textureScale = super.getUniformLocation("textureScale");
        chunkScale = super.getUniformLocation("chunkScale");
        randVector2D = super.getUniformLocation("randVector2D");
        wFactor = super.getUniformLocation("wFactor");
        resetEverything = super.getUniformLocation("resetEverything");
        oldCameraPos = super.getUniformLocation("oldCameraPos");
        renderingFractal = super.getUniformLocation("renderingFractal");
    }

    public void loadVariables() {
        ShaderProgram.load2DVector(displayRes, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
        ShaderProgram.load2DVector(viewportRes, camera.getViewportResolution());
        ShaderProgram.load3DVector(chunkScale, new Vector3D(mapChunkSize));
    }

    public void loadCameraVariables() {
        ShaderProgram.loadMatrix(viewMatrix, camera.getViewMatrix());
        ShaderProgram.load3DVector(lookFrom, camera.getPosition());
        ShaderProgram.load3DVector(lookTo, player.getPosition());
        ShaderProgram.load3DVector(textureScale, world.getBufferScale().toVector3D());
        ShaderProgram.loadBoolean(renderingFractal, GlobalVariables.renderingFractal);

        ShaderProgram.loadFloat(wFactor, camera.getwFactor());
    }

    public void loadOldCameraPos() {
        ShaderProgram.load3DVector(oldCameraPos, camera.getPosition());
    }

    public void loadOldMatrices() {
        ShaderProgram.loadMatrix(oldMVPMatrix, new Matrix4f(camera.getProjectionViewMatrix()));
        ShaderProgram.loadMatrix(oldViewMatrix, new Matrix4f(camera.getViewMatrix()));
    }

    public void loadRandomVector() {
        ShaderProgram.load2DVector(randVector2D, new Vector2f(rand.nextFloat(), rand.nextFloat()));
    }

    public void setResetEverything(final boolean reset) {
        ShaderProgram.loadBoolean(resetEverything, reset);
    }
}
