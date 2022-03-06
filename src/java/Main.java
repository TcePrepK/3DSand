import core.ImGuiManager;
import core.Loader;
import display.DisplayManager;
import game.Player;
import game.World;
import org.lwjgl.glfw.GLFW;
import renderers.MasterRenderer;
import toolbox.Keyboard;
import toolbox.Logger;
import toolbox.Mouse;
import toolbox.MousePicker;
import toolbox.Points.Point3D;

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
        for (int i = 0; i < chunkViewDistance * 2; i++) {
            for (int j = 0; j < chunkViewDistance * 2; j++) {
                for (int k = 0; k < chunkViewDistance * 2; k++) {
                    world.addChunkToGenerationList(new Point3D(i, j, k));
                }
            }
        }
        final int totalChunks = (int) Math.pow(2 * chunkViewDistance, 3);
        // World Generation

        // Test
//        final BitManager manager = new BitManager(256, 256, 256, 4);
//
//        manager.writeValue(new Vector3D(1, 0, 0), 0b1111);
//        manager.writeValue(new Vector3D(0, 1, 0), 0b1111);
//        manager.writeValue(new Vector3D(0, 0, 1), 0b1111);

//        System.out.println(manager.readByte(0));
        // Test

        // Game Loop
        Logger.out("~ First Frame Starting");
        while (!GLFW.glfwWindowShouldClose(DisplayManager.getWindow())) {
            currentFrame++;

            player.update();
            elementPlacer.update();
            Mouse.update();

//            world.update();

            final double generationTime = world.updateChunkGenerationList();
            worldGenerationPercentage = (totalChunks - world.getChunkGenerationList().size()) / (float) totalChunks * 100;

            World.updateBuffers();

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
