package display;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class DisplayManager {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static final int FPS_CAP = 120;

    private static long lastFrameTime;
    private static float delta;
    private static float framesPerSecond;

    private static long windowID;

    public static void createDisplay() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init failed");
        }

        DisplayManager.windowID = glfwCreateWindow(DisplayManager.WIDTH, DisplayManager.HEIGHT, "Game", MemoryUtil.NULL, MemoryUtil.NULL);
        if (DisplayManager.windowID == MemoryUtil.NULL) {
            throw new IllegalStateException("Window failed");
        }

        final Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
        glfwSetWindowPos(DisplayManager.windowID, (monitor.width - DisplayManager.WIDTH) / 2, (monitor.height - DisplayManager.HEIGHT) / 2);
        glfwMakeContextCurrent(DisplayManager.windowID);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwShowWindow(DisplayManager.windowID);

        GL11.glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        DisplayManager.lastFrameTime = DisplayManager.getCurrentTime();
    }

    public static void updateDisplay() {
        glfwSwapBuffers(DisplayManager.windowID);
        glfwPollEvents();

        final long currentFrameTime = DisplayManager.getCurrentTime();
        DisplayManager.delta = (currentFrameTime - DisplayManager.lastFrameTime) / 1000f;
        DisplayManager.lastFrameTime = currentFrameTime;
        DisplayManager.framesPerSecond = 1 / DisplayManager.delta;
    }

    public static float getFrameTimeSeconds() {
        return DisplayManager.delta;
    }

    public static float getFPS() {
        return DisplayManager.framesPerSecond;
    }

    public static void closeDisplay() {
        glfwDestroyWindow(DisplayManager.windowID);
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
//        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }

    public static long getWindow() {
        return DisplayManager.windowID;
    }
}
