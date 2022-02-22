import core.ImGuiManager;
import core.Loader;
import display.DisplayManager;
import game.Player;
import org.lwjgl.glfw.GLFW;
import renderers.MasterRenderer;
import toolbox.*;
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
        for (int i = -chunkViewDistance; i < chunkViewDistance; i++) {
            for (int j = -chunkViewDistance; j < chunkViewDistance; j++) {
                for (int k = -chunkViewDistance; k < chunkViewDistance; k++) {
                    world.addChunkToGenerationList(new Point3D(i, j, k));
                }
            }
        }
        final int totalChunks = (int) Math.pow(2 * chunkViewDistance, 3);
        // World Generation

        // Test
//        final BitManager manager = new BitManager(256, 256, 256, 1);

//        manager.writeValue(0, 0b1111);
//        manager.writeValue(1, 0b1111);
//        manager.writeValue(2, 0b1111);

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
//            world.updateBuffer();
            final double generationTime = world.updateChunkGenerationList();
            worldGenerationPercentage = (totalChunks - world.chunkGenerationList.size()) / (float) totalChunks * 100;

            if (worldGenerationPercentage != 100 && !Maths.closeEnough(worldGenerationPercentage, 0, 1) && Maths.closeEnough(worldGenerationPercentage % 25, 0, 1)) {
                renderer.recreateWorldTexture = true;
            }

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
