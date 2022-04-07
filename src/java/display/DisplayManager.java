package display;

import core.Signal;
import core.Timer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import toolbox.Logger;

import java.awt.*;

import static core.GlobalVariables.currentFrame;
import static org.lwjgl.glfw.GLFW.*;

public class DisplayManager {
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;

    private static final Timer fpsTimer = new Timer();
    private static float delta;
    private static float FPS;
    private static float AverageFPS;

    private static long windowID;

    public static final Signal screenSizeChange = new Signal();

    public static void createDisplay() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init failed");
        }
        Logger.out("~ GLFW Initialized Successfully");

        DisplayManager.windowID = glfwCreateWindow(DisplayManager.WIDTH, DisplayManager.HEIGHT, "Game", MemoryUtil.NULL, MemoryUtil.NULL);
        if (DisplayManager.windowID == MemoryUtil.NULL) {
            throw new IllegalStateException("Window failed");
        }
        Logger.out("~ Windows Created Successfully");

        glfwSetWindowSizeCallback(DisplayManager.windowID, DisplayManager::screenResize);

        final Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
        glfwSetWindowPos(DisplayManager.windowID, (monitor.width - DisplayManager.WIDTH) / 2, (monitor.height - DisplayManager.HEIGHT) / 2);
        glfwMakeContextCurrent(DisplayManager.windowID);
        GL.createCapabilities();
        glfwSwapInterval(0);
        glfwShowWindow(DisplayManager.windowID);

        GL11.glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        Logger.out("~ Viewport Set Successfully");

        DisplayManager.fpsTimer.startTimer();
    }

    public static void updateDisplayTimer() {
        DisplayManager.delta = (float) DisplayManager.fpsTimer.stopTimer();
        DisplayManager.FPS = 1 / DisplayManager.delta;
        DisplayManager.AverageFPS = ((DisplayManager.AverageFPS * (currentFrame - 1)) + DisplayManager.FPS) / currentFrame;

        DisplayManager.fpsTimer.startTimer();
    }

    public static void closeDisplay() {
        Callbacks.glfwFreeCallbacks(DisplayManager.windowID);
        glfwDestroyWindow(DisplayManager.windowID);
        glfwTerminate();
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
        return DisplayManager.FPS;
    }

    public static float getAverageFPS() {
        return DisplayManager.AverageFPS;
    }

    public static float getDelta() {
        return DisplayManager.delta;
    }

    public static long getWindow() {
        return DisplayManager.windowID;
    }
}
