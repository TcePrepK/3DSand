package game;

import toolbox.Points.Point3D;
import toolbox.Timer;

import java.util.ArrayList;
import java.util.List;

import static core.GlobalVariables.*;

public class World {
    private final List<Point3D> chunkGenerationList = new ArrayList<>();

    //    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize, mapChunkSize, 2 * chunkViewDistance * mapChunkSize);
    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize);

    private double totalGenerationTime;

//    public void update() {
//        for (int i = 0; i < chunkUpdateList.size(); i++) {
//            boolean updatedThisChunk = false;
//
//            final Chunk chunk = chunkUpdateList.get(i);
//            for (int x = chunk.getMinX(); x < chunk.getMaxX(); x++) {
//                for (int y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
//                    for (int z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) {
//                        final Element e = chunk.getElement(x, y, z);
//
//                        if (e == null) {
//                            continue;
//                        }
//
//                        final boolean updatedThisElement = e.update(new Point3D(x, y, z));
//                        if (updatedThisElement) {
//                            updatedThisChunk = true;
//                        }
//                    }
//                }
//            }
//
//            if (!updatedThisChunk) {
//                chunkUpdateList.remove(chunk);
//                i--;
//            }
//
//            chunk.updateRect(updatedThisChunk);
//        }
//    }

    public static void updateBuffers() {
        chunkManager.updateBuffers();
    }

    public double updateChunkGenerationList() {
        if (!generateWorld || chunkGenerationList.isEmpty()) {
            return totalGenerationTime;
        }

        final Timer generationTimer = new Timer();
        generationTimer.startTimer();

        int generatedChunkAmount = 0;
        final int targetAmount = ChunkGenerationSpeed.enumToSpeed(generationSpeedOption);
        while (generationTimer.getTime() < 1 && generatedChunkAmount++ < targetAmount) {
            if (chunkGenerationList.isEmpty()) {
                break;
            }

            final int randomIndex = rand.nextInt(chunkGenerationList.size());
            final Point3D selectedPos = chunkGenerationList.get(randomIndex);
            chunkManager.createChunkChunkSpace(selectedPos);
            chunkGenerationList.remove(selectedPos);
        }
        totalGenerationTime += generationTimer.stopTimer();
        return totalGenerationTime;
    }

    public void addChunkToGenerationList(final Point3D pos) {
        chunkGenerationList.add(pos);
    }

    public List<Point3D> getChunkGenerationList() {
        return chunkGenerationList;
    }

    public Point3D getWorldScale() {
        return worldScale;
    }
}
