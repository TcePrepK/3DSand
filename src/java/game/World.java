package game;

import elements.Element;
import org.lwjgl.BufferUtils;
import toolbox.Points.Point3D;
import toolbox.Timer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static core.GlobalVariables.*;

public class World {
    private final List<Point3D> chunkGenerationList = new ArrayList<>();

    //    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize, mapChunkSize, 2 * chunkViewDistance * mapChunkSize);
    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize);

    public final ByteBuffer worldBuffer = BufferUtils.createByteBuffer(worldScale.x * worldScale.y * worldScale.z);

    private final int bitmaskSize = 4;
    private final Point3D bitmaskScale = worldScale.div(bitmaskSize);
    private final int[] bitmaskSizeGrid = new int[bitmaskScale.x * bitmaskScale.y * bitmaskScale.z];
    private final ByteBuffer bitmaskGrid = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);

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
        if (chunkGenerationList.isEmpty()) {
            return totalGenerationTime;
        }

        final Timer generationTimer = new Timer();
        generationTimer.startTimer();
        while (generationTimer.getTime() < 0.1) {
            if (chunkGenerationList.isEmpty()) {
                renderer.recreateWorldTexture = true;
                break;
            }

            final int randomIndex = rand.nextInt(chunkGenerationList.size());
            final Point3D selectedPos = chunkGenerationList.get(randomIndex);
            chunkManager.createChunkChunkSpace(selectedPos);
            chunkGenerationList.remove(selectedPos);

            renderer.recreateWorldTexture = false;
        }
        totalGenerationTime += generationTimer.stopTimer();
        return totalGenerationTime;
    }

    public void addChunkToGenerationList(final Point3D pos) {
        chunkGenerationList.add(pos);
    }

    public int getBufferIDX(final int x, final int y, final int z) {
        return x + (y * worldScale.x) + (z * worldScale.x * worldScale.y);
    }

    public int getBitmaskIdx(final int x, final int y, final int z) {
        final int fX = x / bitmaskSize;
        final int fY = y / bitmaskSize;
        final int fZ = z / bitmaskSize;
        return fX + (fY * bitmaskScale.x) + (fZ * bitmaskScale.x * bitmaskScale.y);
    }

    public void setBufferElement(final int x, final int y, final int z, final Element e) {
        if (x < 0 || y < 0 || z < 0 || x >= worldScale.x || y >= worldScale.y || z >= worldScale.z) {
//            System.out.println("Error! Tried to place element to: " + new Point3D(oX, oY, oZ));
            return;
        }

        final int worldIDX = getBufferIDX(x, y, z);
        final byte replacedID = worldBuffer.get(worldIDX);
        final byte newID = e == null ? 0 : (byte) e.getId();
        worldBuffer.put(worldIDX, newID);

        if (replacedID == newID) {
            return;
        }

        final int bitmaskIDX = getBitmaskIdx(x, y, z);
        bitmaskSizeGrid[bitmaskIDX] += newID == 0 ? -1 : 1;
        bitmaskGrid.put(bitmaskIDX, (byte) ((bitmaskSizeGrid[bitmaskIDX] == 0) ? 0 : 1));

        renderer.recreateWorldTexture = true;
    }

    public List<Point3D> getChunkGenerationList() {
        return chunkGenerationList;
    }

    public ByteBuffer getWorldBuffer() {
        return worldBuffer;
    }

    public Point3D getWorldScale() {
        return worldScale;
    }

    public ByteBuffer getBitmaskGrid() {
        return bitmaskGrid;
    }

    public Point3D getBitmaskScale() {
        return bitmaskScale;
    }

    public int getBitmaskSize() {
        return bitmaskSize;
    }
}
