package core;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import toolbox.Signal;
import toolbox.Timer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class DisplayManager {
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    private static final int FPS_CAP = 120;

    private static final Timer timer = new Timer();
    private static float delta;
    private static float framesPerSecond;

    private static long windowID;

    public static final Signal screenSizeChange = new Signal();

    public static void createDisplay() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init failed");
        }

        DisplayManager.windowID = glfwCreateWindow(DisplayManager.WIDTH, DisplayManager.HEIGHT, "Game", MemoryUtil.NULL, MemoryUtil.NULL);
        if (DisplayManager.windowID == MemoryUtil.NULL) {
            throw new IllegalStateException("Window failed");
        }

        glfwSetWindowSizeCallback(DisplayManager.windowID, DisplayManager::screenResize);

        final Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
        glfwSetWindowPos(DisplayManager.windowID, (monitor.width - DisplayManager.WIDTH) / 2, (monitor.height - DisplayManager.HEIGHT) / 2);
        glfwMakeContextCurrent(DisplayManager.windowID);
        GL.createCapabilities();
        glfwSwapInterval(0);
        glfwShowWindow(DisplayManager.windowID);

        GL11.glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);

        DisplayManager.timer.startTimer();
    }

    public static void updateDisplay() {
        glfwSwapBuffers(DisplayManager.windowID);
        glfwPollEvents();

        DisplayManager.delta = (float) DisplayManager.timer.stopTimer();
        DisplayManager.framesPerSecond = 1 / DisplayManager.delta;
        DisplayManager.timer.startTimer();
    }

    private static void screenResize(final long window, final int width, final int height) {
        DisplayManager.WIDTH = width;
        DisplayManager.HEIGHT = height;
        DisplayManager.screenSizeChange.dispatch();
    }

    public static float getFrameTimeSeconds() {
        return DisplayManager.delta;
    }

    public static float getFPS() {
        return DisplayManager.framesPerSecond;
    }

    public static void closeDisplay() {
        Callbacks.glfwFreeCallbacks(DisplayManager.windowID);
        glfwDestroyWindow(DisplayManager.windowID);
        glfwTerminate();
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
//        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }

    public static long getWindow() {
        return DisplayManager.windowID;
    }
}
