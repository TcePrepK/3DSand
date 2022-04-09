package game.threads;

import core.BasicThread;
import elements.Element;
import elements.ElementRegistry;
import game.Chunk;
import toolbox.Points.Point3D;

import java.util.List;

import static core.GlobalVariables.*;

public class ChunkUpdateThread extends BasicThread {
    public ChunkUpdateThread() {
        super("chunkUpdateThread");

        threadManager.getThread("chunkGenerationThread").whenDied(this::start);
    }

    @Override
    protected void loop() {
        if (!updateWorld) {
            return;
        }

        final List<Chunk> chunkUpdateList = chunkManager.getChunkUpdateList();
        if (chunkUpdateList.isEmpty()) {
            return;
        }

        final int randomIndex = rand.nextInt(chunkUpdateList.size());
        final Chunk chunk = chunkUpdateList.get(randomIndex);
        if (chunk.isEmpty()) {
            return;
        }

        boolean updatedThisChunk = false;
//        for (int x = chunk.getMinX(); x < chunk.getMaxX(); x++) {
//            for (int y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
//                for (int z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) {
        for (int x = chunk.getX(); x < chunk.getX() + mapChunkSize; x++) {
            for (int y = chunk.getY(); y < chunk.getY() + mapChunkSize; y++) {
                for (int z = chunk.getZ(); z < chunk.getZ() + mapChunkSize; z++) {
                    final Element element = chunk.getElement(x, y, z);

                    if (element.equals(ElementRegistry.emptyElement)) {
                        continue;
                    }

                    if (element.update(new Point3D(x, y, z))) {
                        updatedThisChunk = true;
                    }
                }
            }
        }

        if (!updatedThisChunk) {
            chunkUpdateList.remove(randomIndex);
        }

        chunk.updateRect();
    }
}
