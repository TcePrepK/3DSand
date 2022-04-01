package game;

import toolbox.Points.Point3D;

import java.util.ArrayList;
import java.util.List;

import static core.GlobalVariables.*;

public class World {
    private final List<Point3D> chunkGenerationList = new ArrayList<>();

    //    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize, mapChunkSize, 2 * chunkViewDistance * mapChunkSize);
    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize);

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
