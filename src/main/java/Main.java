import core.ImGuiManager;
import core.Loader;
import core.MasterRenderer;
import display.DisplayManager;
import game.Player;
import org.lwjgl.glfw.GLFW;
import toolbox.Keyboard;
import toolbox.Mouse;
import toolbox.MousePicker;
import toolbox.Timer;

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

        // World Generation
        final Timer timer = new Timer();
        timer.startTimer();
        for (int i = -chunkViewDistance; i < chunkViewDistance; i++) {
            for (int j = -chunkViewDistance; j < chunkViewDistance; j++) {
                for (int m = -chunkViewDistance; m < chunkViewDistance; m++) {
                    world.getChunkOrCreate(i * mapChunkSize, j * mapChunkSize, m * mapChunkSize);
                }
            }
        }
        final double generationTime = timer.stopTimer();
        // World Generation

        // Game Loop
        while (!GLFW.glfwWindowShouldClose(DisplayManager.getWindow())) {
            currentFrame++;

            player.update();
            elementPlacer.update();
            Mouse.update();

//            world.update();
//            world.updateBuffer();

            DisplayManager.startRenderTimer();
            renderer.render();
            imGuiManager.update(generationTime, DisplayManager.getRenderTime());
            DisplayManager.updateDisplay();
        }

        renderer.cleanUp();
        loader.cleanUp();
        imGuiManager.cleanUp();
        DisplayManager.closeDisplay();
    }
}
