package shaders;

import display.DisplayManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import toolbox.Vector3D;

import static core.GlobalVariables.*;

public class RayTracerBaseShader extends BaseShader {
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
    private int resetEverything;
    private int oldCameraPos;
    private int bitmaskSize;
    private int isPathTracing;

    public RayTracerBaseShader() {
        super(RayTracerBaseShader.VERTEX_FILE, RayTracerBaseShader.FRAGMENT_FILE);
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
        resetEverything = super.getUniformLocation("resetEverything");
        oldCameraPos = super.getUniformLocation("oldCameraPos");
        bitmaskSize = super.getUniformLocation("bitmaskSize");
        isPathTracing = super.getUniformLocation("isPathTracing");
    }

    public void loadResolutions() {
        BaseShader.load2DVector(displayRes, new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT));
        BaseShader.load2DVector(viewportRes, camera.getViewportResolution());
        BaseShader.load3DVector(chunkScale, new Vector3D(mapChunkSize));
    }

    public void loadCameraVariables() {
        BaseShader.loadMatrix(viewMatrix, camera.getViewMatrix());
        BaseShader.load3DVector(lookFrom, camera.getPosition());
        BaseShader.load3DVector(lookTo, player.getPosition());
        BaseShader.load3DVector(textureScale, world.getBufferScale().toVector3D());
    }

    public void loadBitmaskSize(final int size) {
        BaseShader.loadInt(bitmaskSize, size);
    }

    public void loadOldVariables() {
        BaseShader.load3DVector(oldCameraPos, camera.getPosition());
        BaseShader.loadMatrix(oldMVPMatrix, new Matrix4f(camera.getProjectionViewMatrix()));
        BaseShader.loadMatrix(oldViewMatrix, new Matrix4f(camera.getViewMatrix()));
    }

    public void loadPathTracingSetting() {
        BaseShader.loadBoolean(isPathTracing, pathTracing);
    }

    public void loadRandomVector() {
        BaseShader.load2DVector(randVector2D, new Vector2f(rand.nextFloat(), rand.nextFloat()));
    }

    public void setResetEverything(final boolean reset) {
        BaseShader.loadBoolean(resetEverything, reset);
    }
}
