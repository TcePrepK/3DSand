package toolbox;

import core.Camera;
import core.World;
import display.DisplayManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MousePicker {
    private static final int RECURSION_COUNT = 200;
    private static final float RAY_RANGE = 600;

    private Vector3f currentRay;

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Camera camera;

    private Vector3f currentWorldPoint;

    public MousePicker(final Camera camera) {
        this.camera = camera;
        projectionMatrix = camera.getProjectionMatrix();
        viewMatrix = camera.getViewMatrix();
    }

    public Vector3f getCurrentRay() {
        return currentRay;
    }

    public void update() {
        currentRay = calculateMouseRay();
        if (intersectionInRange(0, MousePicker.RAY_RANGE, currentRay)) {
            currentWorldPoint = binarySearch(0, 0, MousePicker.RAY_RANGE, currentRay);
        } else {
            currentWorldPoint = null;
        }
    }

    private Vector3f calculateMouseRay() {
        final float mouseX = Mouse.x();
        final float mouseY = DisplayManager.HEIGHT - Mouse.y();

        return camera.getRay(mouseX, mouseY).toVector3f();
//        final Vector2f normalizedCoords = MousePicker.getNormalizedDeviceCoords(mouseX, mouseY);
//        final Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1, 1);
//        return toWorldCoords(clipCoords);
    }

//    private Vector3f toWorldCoords(final Vector4f clipCoords) {
//        final Vector4f eyeCoords = projectionMatrix.invert().transform(clipCoords);
//        final Vector4f rayWorld = viewMatrix.invert().transform(new Vector4f(eyeCoords.x, eyeCoords.y, -1, 0));
//        final Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
//        mouseRay.normalize();
//        return mouseRay;
//    }

//    private static Vector2f getNormalizedDeviceCoords(final float mouseX, final float mouseY) {
//        final float x = (2 * mouseX) / DisplayManager.WIDTH - 1;
//        final float y = (2 * mouseY) / DisplayManager.HEIGHT - 1;
//        return new Vector2f(x, y);
//    }

    private Vector3f getPointOnRay(final Vector3f ray, final float distance) {
        final Vector3D camPos = camera.getPosition();
        final Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        final Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);

        return start.add(scaledRay);
    }

    private Vector3f binarySearch(final int count, final float start, final float finish, final Vector3f ray) {
        final float half = start + ((finish - start) / 2);
        if (count >= MousePicker.RECURSION_COUNT) {
            final Vector3f endPoint = getPointOnRay(ray, half);
            if (!World.outBounds((int) Math.floor(endPoint.x()), 0, (int) Math.floor(endPoint.z()))) {
                return endPoint;
            } else {
                return null;
            }
        }
        if (intersectionInRange(start, half, ray)) {
            return binarySearch(count + 1, start, half, ray);
        } else {
            return binarySearch(count + 1, half, finish, ray);
        }
    }

    private boolean intersectionInRange(final float start, final float finish, final Vector3f ray) {
        final Vector3f startPoint = getPointOnRay(ray, start);
        final Vector3f endPoint = getPointOnRay(ray, finish);
        return !MousePicker.isUnderGround(startPoint) && MousePicker.isUnderGround(endPoint);
    }

    private static boolean isUnderGround(final Vector3f testPoint) {
        return testPoint.y < 0;
    }

    public Vector3f getCurrentWorldPoint() {
        return currentWorldPoint;
    }
}
