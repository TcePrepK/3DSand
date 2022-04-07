import core.Keyboard;
import core.Mouse;
import display.DisplayManager;
import elements.ElementRegistry;
import game.Player;
import game.World;
import game.threads.ChunkGenerationThread;
import game.threads.ChunkUpdateThread;
import org.lwjgl.glfw.GLFW;
import renderers.MasterRenderer;
import toolbox.Logger;
import toolbox.MousePicker;
import toolbox.Points.Point3D;

import static core.GlobalVariables.*;

public class Main {
    public static void main(final String[] args) {
        DisplayManager.createDisplay();

        // Init
        ElementRegistry.init();
        elementPlacer.init();
        Keyboard.init();
        Mouse.init();
        // Init

        // Camera
        player = new Player(camera);
        mousePicker = new MousePicker(camera);
        // Camera

        // World Generation
        for (int i = 0; i < chunkViewDistance * 2; i++) {
            for (int j = 0; j < chunkViewDistance * 2; j++) {
                for (int k = 0; k < chunkViewDistance * 2; k++) {
                    world.addChunkToGenerationList(new Point3D(i, j, k));
                }
            }
        }
        // World Generation

        // Thread
        threadManager.addThread(new ChunkGenerationThread()).start();
        threadManager.addThread(new ChunkUpdateThread());
        // Thread

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
            threadManager.update();

            World.updateBuffers();

            DisplayManager.startRenderTimer();
            renderer.render();
            imGuiManager.update(DisplayManager.getRenderTime());
            MasterRenderer.finishRendering();

            renderer.loadOldCameraVariables();
            DisplayManager.updateDisplay();
        }

        threadManager.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        imGuiManager.cleanUp();
        DisplayManager.closeDisplay();
    }
}
