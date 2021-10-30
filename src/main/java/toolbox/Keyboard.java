package toolbox;

import core.Signal;
import display.DisplayManager;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Keyboard {
    public static final List<String> pressedList = new ArrayList<>();

    public static final Signal keyPressed = new Signal();

    public static void init() {
        GLFW.glfwSetKeyCallback(DisplayManager.getWindow(), (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }

            if (action == GLFW.GLFW_PRESS) {
                Keyboard.pressedList.add(GLFW.glfwGetKeyName(key, scancode));
            } else if (action == GLFW.GLFW_RELEASE) {
                Keyboard.pressedList.remove(GLFW.glfwGetKeyName(key, scancode));
            }

            Keyboard.keyPressed.test();
        });
    }

    public static boolean isKeyDown(final String key) {
        return Keyboard.pressedList.contains(key.toLowerCase());
    }
}
