package game;

import core.BasicThread;
import elements.Element;
import toolbox.Points.Point3D;

import java.util.List;

import static core.GlobalVariables.chunkManager;
import static core.GlobalVariables.threadManager;

public class ChunkUpdateThread extends BasicThread {
    public ChunkUpdateThread() {
        super("chunkUpdateThread");

        final BasicThread generationThread = threadManager.getThread("chunkGenerationThread");
        if (generationThread == null) {
            return;
        }

        generationThread.whenDied(this::start);
    }

    @Override
    public void run() {
        while (true) {
            final List<Chunk> chunkUpdateList = chunkManager.getChunkUpdateList();
            for (int i = 0; i < chunkUpdateList.size(); i++) {
                boolean updatedThisChunk = false;

                final Chunk chunk = chunkUpdateList.get(i);
                for (int x = chunk.getMinX(); x < chunk.getMaxX(); x++) {
                    for (int y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
                        for (int z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) {
                            final Element e = chunk.getElement(x, y, z);

                            if (e == null) {
                                continue;
                            }

                            final boolean updatedThisElement = e.update(new Point3D(x, y, z));
                            if (updatedThisElement) {
                                updatedThisChunk = true;
                            }
                        }
                    }
                }

                if (!updatedThisChunk) {
                    chunkUpdateList.remove(chunk);
                    i--;
                }

                chunk.updateRect(updatedThisChunk);
            }

            breath();
        }
    }
}
