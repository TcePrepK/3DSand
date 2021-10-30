package toolbox;

import core.Signal;
import display.DisplayManager;
import org.lwjgl.glfw.GLFW;

public class Mouse {
    private static int x = 0;
    private static int y = 0;

    private static int dx = 0;
    private static int dy = 0;

    public static final boolean[] pressedList = new boolean[8];

    public static final Signal mouseLeftDown = new Signal();
    public static final Signal mouseMiddleMove = new Signal();
    public static final Signal mouseRightDown = new Signal();

    public static void init() {
        GLFW.glfwSetMouseButtonCallback(DisplayManager.getWindow(), (window, button, action, mods) -> {
            if (button >= Mouse.pressedList.length) {
                return;
            }

            if (action == GLFW.GLFW_PRESS) {
                Mouse.pressedList[button] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                Mouse.pressedList[button] = false;
            }
        });

        GLFW.glfwSetScrollCallback(DisplayManager.getWindow(), (window, offX, offY) -> {
            Mouse.mouseMiddleMove.dispatch((int) offY);
        });
    }

    public static void update() {
        final double[] posX = new double[1];
        final double[] posY = new double[1];

        GLFW.glfwGetCursorPos(DisplayManager.getWindow(), posX, posY);

        Mouse.dx = (int) posX[0] - Mouse.x;
        Mouse.dy = (int) posY[0] - Mouse.y;
        Mouse.x = (int) posX[0];
        Mouse.y = (int) posY[0];

        if (Mouse.isButtonDown(0)) {
            Mouse.mouseLeftDown.dispatch();
        } else if (Mouse.isButtonDown(1)) {
            Mouse.mouseRightDown.dispatch();
        }
    }

    public static boolean isButtonDown(final int button) {
        if (button >= Mouse.pressedList.length) {
            return false;
        }

        return Mouse.pressedList[button];
    }

    public static int x() {
        return Mouse.x;
    }

    public static int y() {
        return Mouse.y;
    }

    public static int dx() {
        return Mouse.dx;
    }

    public static int dy() {
        return Mouse.dy;
    }
}
