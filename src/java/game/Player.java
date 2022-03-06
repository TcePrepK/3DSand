package game;

import core.GlobalVariables;
import display.DisplayManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import toolbox.Keyboard;
import toolbox.Vector3D;

import static core.GlobalVariables.world;
import static toolbox.Maths.rotateVector2D;

public class Player {
    private static final float RUN_SPEED = 20;
    private static final float GRAVITY = -5;
    private static final float JUMP_POWER = 5;
    private final float HEIGHT = 0;

    private final Vector3D position = world.getWorldScale().div(2).toVector3D();
    private final Vector2f[] hitboxPoints = {
            new Vector2f(-0.5f, -0.5f),
            new Vector2f(0.5f, -0.5f),
            new Vector2f(-0.5f, 0.5f),
            new Vector2f(0.5f, 0.5f)
    };

    private final Vector3f desiredMovement = new Vector3f();

    private boolean isInAir = false;

    private final Camera camera;

    public Player(final Camera camera) {
        this.camera = camera;

        Keyboard.keyPressed.add(() -> GlobalVariables.freePlayMode = false, "J");
        Keyboard.keyPressed.add(() -> GlobalVariables.freePlayMode = true, "K");
    }

    public void update() {
        float gravity = Player.GRAVITY;
        if (GlobalVariables.creativeMode) {
            gravity = 0;
            // Player.RUN_SPEED = 80;
            // Player.JUMP_POWER = 80;
        }

        checkInputs();
        final float dt = DisplayManager.getFrameTimeSeconds() * 2.5f;

        final float forwardDistance = desiredMovement.z * dt;
        final float sidewayDistance = desiredMovement.x * dt;
        final Vector2f directionVector = Player.calculateRotatedDot(forwardDistance, sidewayDistance, camera.getAngleAroundPlayer() + 180);
        increasePosition(directionVector.x, 0, directionVector.y);

        if (GlobalVariables.creativeMode) {
//            camera.getPosition().z += forwardDistance;
//            camera.getPosition().x += sidewayDistance;
            // camera.getPosition().y += 0.005f;
        }

//        desiredMovement.y += gravity * dt;
        increasePosition(0, desiredMovement.y * dt, 0);

//        if (!GlobalVariables.creativeMode) {
//            for (final Vector2f hitpoint : hitboxPoints) {
//                int currentHeight = 0;
//                final int x = (int) Math.floor(position.x + hitpoint.x());
//                final int y = (int) Math.floor(position.y);
//                final int z = (int) Math.floor(position.z + hitpoint.y());
//                final Chunk chunk = World.getChunkAtTile(x, y, z, false);
//                if (chunk != null) {
//                    currentHeight = chunk.getMinHeight(x, z);
//                }
//
//                if (position.y < currentHeight) {
//                    position.y = currentHeight;
//                    isInAir = false;
//                }
//            }
//        }

        camera.update();
        calculateCameraPosition();

        camera.calculateMatrices();
        camera.calculateVariables(position);
    }

    private void calculateCameraPosition() {
        final float theta = camera.getAngleAroundPlayer();
        camera.setYaw(180 - theta);

        if (!GlobalVariables.freePlayMode) {
            camera.getPosition().set(position.x, position.y, position.z);
        } else {
            final float horizontalDistance = camera.calculateHorizontalDistance();
            final float verticalDistance = camera.calculateVerticalDistance();
            final float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
            final float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));

            camera.getPosition().set(position.x - offsetX, position.y + verticalDistance, position.z - offsetZ);
        }
    }

    private void jump() {
        if (GlobalVariables.creativeMode) {
            increasePosition(0, Player.JUMP_POWER * DisplayManager.getFrameTimeSeconds(), 0);
            return;
        }

        if (isInAir) {
            return;
        }

        desiredMovement.y = Player.JUMP_POWER;
        isInAir = true;
    }

    private void checkInputs() {
        desiredMovement.x = 0;
        desiredMovement.z = 0;

        if (Keyboard.isKeyDown("W")) {
            desiredMovement.z = -Player.RUN_SPEED;
        } else if (Keyboard.isKeyDown("S")) {
            desiredMovement.z = Player.RUN_SPEED;
        }

        if (Keyboard.isKeyDown("A")) {
            desiredMovement.x = -Player.RUN_SPEED;
        } else if (Keyboard.isKeyDown("D")) {
            desiredMovement.x = Player.RUN_SPEED;
        }

        if (Keyboard.isKeyDown(Keyboard.SPACE)) {
            desiredMovement.y += 1;
        } else if (Keyboard.isKeyDown(Keyboard.LSHIFT)) {
            desiredMovement.y -= 1;
        } else {
            desiredMovement.y = 0;
        }

//        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
//            jump();
//        } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && GlobalVariables.creativeMode) {
//            increasePosition(0, -Player.JUMP_POWER * DisplayManager.getFrameTimeSeconds(), 0);
//        }
    }

    private static Vector2f calculateRotatedDot(final float forward, final float sideways, final float angle) {
        if (forward == 0) {
            return rotateVector2D(new Vector2f(sideways, 0), -angle);
        } else if (sideways == 0) {
            return rotateVector2D(new Vector2f(0, forward), -angle);
        }

        final Vector2f middleVector = new Vector2f(sideways, forward);
        final Vector2f scaledVector = new Vector2f((float) (middleVector.x / Math.sqrt(2)), (float) (middleVector.y / Math.sqrt(2)));
        return rotateVector2D(scaledVector, -angle);
    }

    public void increasePosition(final float dx, final float dy, final float dz) {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }

    public Vector3D getPosition() {
        return position;
    }
}
