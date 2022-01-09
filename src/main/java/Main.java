import core.DisplayManager;
import core.ImGuiManager;
import core.Loader;
import core.Player;
import display.MasterRenderer;
import org.lwjgl.glfw.GLFW;
import toolbox.Keyboard;
import toolbox.Mouse;
import toolbox.MousePicker;

import static core.GlobalVariables.*;

public class Main {
    public static void main(final String[] args) {
        DisplayManager.createDisplay();

        imGuiManager = new ImGuiManager();
        loader = new Loader();
        renderer = new MasterRenderer();

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
        for (int i = -chunkViewDistance; i < chunkViewDistance; i++) {
            for (int j = -chunkViewDistance; j < chunkViewDistance; j++) {
                for (int m = -chunkViewDistance; m < chunkViewDistance; m++) {
                    world.getChunkOrCreate(i * mapChunkSize, j * mapChunkSize, m * mapChunkSize);
                }
            }
        }
//        world.updateBuffer();
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
//            world.updateBuffer();
            final double u2 = DisplayManager.getCurrentTime();

            renderer.render();
            imGuiManager.update();

            DisplayManager.updateDisplay();
        }

        renderer.cleanUp();
        loader.cleanUp();
        imGuiManager.cleanUp();
        DisplayManager.closeDisplay();
    }
}
