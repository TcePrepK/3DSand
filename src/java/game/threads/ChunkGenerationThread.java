package game.threads;

import core.BasicThread;
import toolbox.Points.Point3D;

import java.util.List;

import static core.GlobalVariables.*;

public class ChunkGenerationThread extends BasicThread {
    private float generatePercentage = 0;

    public ChunkGenerationThread() {
        super("chunkGenerationThread");
    }

    @Override
    protected void loop() {
        if (!generateWorld) {
            return;
        }

        final List<Point3D> chunkGenerationList = world.getChunkGenerationList();
        generatePercentage = (chunkManager.CHUNK_AMOUNT - chunkGenerationList.size()) / (float) chunkManager.CHUNK_AMOUNT * 100;

        if (chunkGenerationList.isEmpty()) {
            kill();
            return;
        }

        final int randomIndex = rand.nextInt(chunkGenerationList.size());
        final Point3D selectedPos = chunkGenerationList.get(randomIndex);
        chunkManager.createChunkChunkSpace(selectedPos);
        chunkGenerationList.remove(selectedPos);

    }

    public float getGeneratePercentage() {
        return generatePercentage;
    }
}