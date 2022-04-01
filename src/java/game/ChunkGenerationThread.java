package game;

import core.BasicThread;
import core.GlobalVariables;
import toolbox.Points.Point3D;

import java.util.List;

import static core.GlobalVariables.*;

public class ChunkGenerationThread extends BasicThread {
    public ChunkGenerationThread() {
        super("chunkGenerationThread");
    }

    @Override
    public void run() {
        final List<Point3D> chunkGenerationList = world.getChunkGenerationList();
        int frameCounter = 0;
        while (!chunkGenerationList.isEmpty()) {
            if (frameCounter++ == 50) {
                breath();
                frameCounter = 0;
            }

            if (!GlobalVariables.generateWorld) {
                continue;
            }

            final int randomIndex = rand.nextInt(chunkGenerationList.size());
            final Point3D selectedPos = chunkGenerationList.get(randomIndex);
            chunkManager.createChunkChunkSpace(selectedPos);
            chunkGenerationList.remove(selectedPos);

            if (chunkGenerationList.size() % 50 == 0) {
                System.out.println(chunkGenerationList.size() + " Chunks Left!");
            }
        }
    }
}