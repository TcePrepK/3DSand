import core.ImGuiManager;
import core.Loader;
import display.DisplayManager;
import game.Player;
import org.lwjgl.glfw.GLFW;
import renderers.MasterRenderer;
import toolbox.*;

import static core.GlobalVariables.*;

public class Main {
    public static void main(final String[] args) {
        DisplayManager.createDisplay();

        imGuiManager = new ImGuiManager();
        loader = new Loader();
        renderer = new MasterRenderer();

        // Init
        elementRegistery.init();
        elementPlacer.init();
        Keyboard.init();
        Mouse.init();
        // Init

        // Camera
        player = new Player(camera);
        mousePicker = new MousePicker(camera);
        // Camera

        // World Generation
        Logger.out("~ World Generation Starting");
        final Timer timer = new Timer();
        timer.startTimer();
        for (int i = -chunkViewDistance; i < chunkViewDistance; i++) {
            for (int j = -chunkViewDistance; j < chunkViewDistance; j++) {
                for (int m = -chunkViewDistance; m < chunkViewDistance; m++) {
                    world.getChunkOrCreate(i * mapChunkSize, j * mapChunkSize, m * mapChunkSize);
                }
            }
            Logger.out("~ World Generation " + Math.floor((i + chunkViewDistance + 1) * 25) + "% Done");
        }
        final double generationTime = timer.stopTimer();
        // World Generation

        // Game Loop
        Logger.out("~ First Frame Starting");
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
            MasterRenderer.finishRendering();
            renderer.loadOldCameraVariables();

            DisplayManager.updateDisplay();
        }

        renderer.cleanUp();
        loader.cleanUp();
        imGuiManager.cleanUp();
        DisplayManager.closeDisplay();
    }
}
