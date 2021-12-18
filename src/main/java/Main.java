import core.Player;
import display.DisplayManager;
import display.DisplayRenderer;
import org.lwjgl.glfw.GLFW;
import renderEngine.Loader;
import toolbox.Keyboard;
import toolbox.Mouse;
import toolbox.MousePicker;

import static core.GlobalVariables.*;

public class Main {
    public static void main(final String[] args) {
        DisplayManager.createDisplay();

        loader = new Loader();
        renderer = new DisplayRenderer();

        // Inits
        elementRegistery.init();
        elementPlacer.init();
        Keyboard.init();
        Mouse.init();
        // Inits

        // Camera
        player = new Player(camera);

        mousePicker = new MousePicker(camera);
        // Camera

        // First chunks :)
        final int w = 4;
        for (float i = -w; i < w; i++) {
            for (float j = -w; j < w; j++) {
                world.getChunkOrCreate((int) (i * mapChunkSize), (int) (j * mapChunkSize));
            }
        }
//        world.getChunkOrCreate(0, 0);
        // First chunks :)

        // Game Loop
        while (!GLFW.glfwWindowShouldClose(DisplayManager.getWindow())) {
            currentFrame++;

            player.update();
            elementPlacer.update();
            Mouse.update();

            final double u1 = DisplayManager.getCurrentTime();
//            world.update();
            world.updateBuffer();
            final double u2 = DisplayManager.getCurrentTime();

            renderer.render();

            DisplayManager.updateDisplay();

            System.out.println("Update took: " + (u2 - u1) / 100 + "sec and FPS is: " + DisplayManager.getFPS());
        }

        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }
}
