package game;

import core.Signal;
import display.DisplayManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import toolbox.Mouse;
import toolbox.Vector3D;

import static core.GlobalVariables.*;
import static display.DisplayManager.screenSizeChange;
import static toolbox.Maths.clamp;

public class Camera {
    public static final float FOV = 90;
    public static final float NEAR_PLANE = 1;
    public static final float FAR_PLANE = 1000;

    private final float MAX_PITCH = 90;
    private final float MIN_PITCH = -90;
    private final float MAX_ZOOMLEVEL = 300;
    private final float MIN_ZOOMLEVEL = 20;

    private final Signal matrixWatcher = new Signal();

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f projectionViewMatrix = new Matrix4f();

    private float cameraAspectRatio = DisplayManager.WIDTH / (float) DisplayManager.HEIGHT;
    private float viewportWidth = 2 * (float) Math.tan(Math.toRadians(Camera.FOV) / 2.0);
    private float viewportHeight = viewportWidth / cameraAspectRatio;

    private Vector3D topLeftCorner;
    private Vector3D xIncrement;
    private Vector3D yIncrement;

    private final Vector3D position = new Vector3D();
    private float angleAroundPlayer = 180;
    private float zoomLevel = 200; // 2
    private float desiredZoomLevel = zoomLevel;
    private float pitch = 0; // 20
    private float yaw = 0;
    private float roll;

    public Camera() {
        createProjectionMatrix();

        Mouse.mouseMiddleMove.add(this::calculateZoom);
        Mouse.mouseRightDown.add(this::calculatePitch);

        matrixWatcher.add(() -> {
            renderer.loadCameraVariablesNextFrame();
        });

        screenSizeChange.add(this::screenResize);
    }

    public void update() {
        updateZoom();

        if ((Mouse.isButtonDown(1) && freePlayMode)) {
            calculateAngleAroundPlayer(0.3f);
        } else if (!freePlayMode) {
            calculateAngleAroundPlayer(0.1f);
        }

        if (mouseLocked) {
            // Mouse.setCursorPosition(DisplayManager.WIDTH / 2, DisplayManager.HEIGHT / 2);
        }
    }

    public void screenResize() {
        cameraAspectRatio = DisplayManager.WIDTH / (float) DisplayManager.HEIGHT;
        viewportWidth = 2 * (float) Math.tan(Math.toRadians(Camera.FOV) / 2.0);
        viewportHeight = viewportWidth / cameraAspectRatio;

        createProjectionMatrix();
    }

    public void calculateVariables(final Vector3D playerPos) {
        final Vector3D cameraDirection = playerPos.sub(position).normalize();
        final Vector3D camRightVector = new Vector3D(viewMatrix.m00(), viewMatrix.m10(), viewMatrix.m20());
        final Vector3D camUpVector = new Vector3D(viewMatrix.m01(), viewMatrix.m11(), viewMatrix.m21());

        topLeftCorner = cameraDirection.sub(camRightVector).mult(viewportWidth / 2).add(camUpVector.mult(viewportHeight / 2));
        xIncrement = camRightVector.mult(viewportWidth).div(DisplayManager.WIDTH);
        yIncrement = camUpVector.mult(-viewportHeight).div(DisplayManager.HEIGHT);
    }

    public Vector3D getRay(final float x, final float y) {
        return topLeftCorner.add(xIncrement.mult(x)).add(yIncrement.mult(y)).normalize();
    }

    private void calculateZoom(final Object delta) {
        if (!freePlayMode) {
            return;
        }

        final float zoomSpeed = 5;
        desiredZoomLevel *= 1 - (int) delta / 100f * zoomSpeed;
//        clampDesiredZoomLevel();
    }

    private void updateZoom() {
        final float deltaZoom = desiredZoomLevel - zoomLevel;
        if (Math.abs(deltaZoom) < 0.5) {
            zoomLevel = desiredZoomLevel;
            return;
        }

        zoomLevel += deltaZoom * DisplayManager.getFrameTimeSeconds() * 10;

        if ((deltaZoom > 0 && zoomLevel > desiredZoomLevel) || (deltaZoom < 0 && zoomLevel < desiredZoomLevel)) {
            zoomLevel = desiredZoomLevel;
        }
    }

    private void calculateAngleAroundPlayer(final float m) {
        final float angleChange;
        if (mouseLocked) {
            angleChange = (Mouse.x() - DisplayManager.WIDTH / 2f) * m;
        } else {
            angleChange = Mouse.dx() * m;
        }

        angleAroundPlayer -= angleChange;

        clampAngleAroundPlayer();
    }

    private void calculatePitch() {
        final float pitchChange;
        if (mouseLocked) {
            pitchChange = (Mouse.y() - DisplayManager.HEIGHT / 2f) * 0.1f;
        } else {
            pitchChange = Mouse.dy() * 0.1f;
        }

        pitch += pitchChange;

        clampPitch();
    }

    public float calculateHorizontalDistance() {
        return (float) (Math.cos(Math.toRadians(pitch)) * zoomLevel);
    }

    public float calculateVerticalDistance() {
        return (float) (Math.sin(Math.toRadians(pitch)) * zoomLevel);
    }

    public void calculateMatrices() {
        final Matrix4f oldViewMatrix = new Matrix4f(viewMatrix);

        // ViewMatrix
        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0));
        viewMatrix.rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0));
        viewMatrix.translate(position.mult(-1).toVector3f());

        // ProjectionViewMatrix
        projectionMatrix.mul(viewMatrix, projectionViewMatrix);

        if (!viewMatrix.equals(oldViewMatrix)) {
            matrixWatcher.dispatch();
        }
    }

    public void createProjectionMatrix() {
        final float aspectRatio = (float) DisplayManager.WIDTH / DisplayManager.HEIGHT;
        final float y_scale = (float) ((1f / Math.tan(Math.toRadians(Camera.FOV / 2f))) * aspectRatio);
        final float x_scale = y_scale / aspectRatio;
        final float frustum_length = Camera.FAR_PLANE - Camera.NEAR_PLANE;

        projectionMatrix.m00(x_scale);
        projectionMatrix.m11(y_scale);
        projectionMatrix.m22(-((Camera.FAR_PLANE + Camera.NEAR_PLANE) / frustum_length));
        projectionMatrix.m23(-1);
        projectionMatrix.m32(-((2 * Camera.NEAR_PLANE * Camera.FAR_PLANE) / frustum_length));
        projectionMatrix.m33(0);

    }

    private void clampYaw() {
        yaw = (yaw + 360) % 360;
    }

    private void clampPitch() {
        pitch = clamp(pitch, MIN_PITCH, MAX_PITCH);
    }

    private void clampZoomLevel() {
        zoomLevel = clamp(zoomLevel, MIN_ZOOMLEVEL, MAX_ZOOMLEVEL);
    }

    private void clampDesiredZoomLevel() {
        desiredZoomLevel = clamp(desiredZoomLevel, MIN_ZOOMLEVEL, MAX_ZOOMLEVEL);
    }

    private void clampAngleAroundPlayer() {
        angleAroundPlayer = angleAroundPlayer % 360;
    }

    void setYaw(final float yaw) {
        this.yaw = yaw;
        clampYaw();
    }

    public Vector3D getPosition() {
        return position;
    }

    float getAngleAroundPlayer() {
        return angleAroundPlayer;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getProjectionViewMatrix() {
        return projectionViewMatrix;
    }

    public Vector2f getViewportResolution() {
        return new Vector2f(viewportWidth, viewportHeight);
    }
}
