package core;

import elements.Element;
import toolbox.Point3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static core.GlobalVariables.*;

public class World {
    private final HashMap<String, Chunk> chunksById = new HashMap<>();
    private final List<Chunk> chunkList = new ArrayList<>();
    private final List<Chunk> chunkUpdateList = new ArrayList<>();

    //    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize, mapChunkSize, 2 * chunkViewDistance * mapChunkSize);
    private final Point3D worldScale = new Point3D(5 * mapChunkSize);
    private final Point3D fractalScale = new Point3D(5 * mapChunkSize);

    public final float[] worldBuffer = new float[worldScale.x * worldScale.y * worldScale.z];
    private Point3D bufferScale = new Point3D();

    public void update() {
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
    }

    public void setBufferSize() {
        if (renderingFractal) {
            bufferScale = fractalScale;
        } else {
            bufferScale = worldScale;
        }
    }

    public int getBufferIDX(final int x, final int y, final int z) {
        return x + (y * worldScale.x) + (z * worldScale.x * worldScale.y);
    }

    public void setBufferElement(final int x, final int y, final int z, final Element e) {
        if (!registerWorldToBuffer) {
            return;
        }

        final int oX = x + worldScale.x / 2;
        final int oY = y;
        final int oZ = z + worldScale.z / 2;

        if (oX < 0 || oY < 0 || oZ < 0 || oX >= worldScale.x || oY >= worldScale.y || oZ >= worldScale.z) {
//            System.out.println("Error! Tried to place element to: " + new Point3D(oX, oY, oZ));
            return;
        }

        worldBuffer[getBufferIDX(oX, oY, oZ)] = e == null ? 0 : e.getId();
    }

    public void updateBuffer() {
        final Point3D cameraPos = camera.getPosition().floor().toPoint3D();
//        final Point startingChunk = World.getChunkPosition(cameraPos.x, cameraPos.z);
        final Point startingChunk = new Point(0, 0);

        final int totalLength = worldScale.x * worldScale.y * worldScale.z;
        final float[] byteList = new float[totalLength];

//        int totalPos = 0;
//        for (int i = -chunkViewDistance; i < chunkViewDistance; i++) {
//            for (int j = -chunkViewDistance; j < chunkViewDistance; j++) {
//                final Point offChunk = new Point(startingChunk.x + i, startingChunk.y + j);
//
//                final Chunk chunk = getChunkWithPoint(offChunk);
//                if (chunk != null) {
//                    System.arraycopy(chunk.getIdGrid(), 0, byteList, totalPos, chunkIdGridSize);
//                }
//
//                totalPos += chunkIdGridSize;
//            }
//        }

//        for (int i = 0; i < chunkViewDistance * 2; i++) {
//            for (int j = 0; j < chunkViewDistance * 2; j++) {
//                final Point offChunk = new Point(startingChunk.x + i - chunkViewDistance, startingChunk.y + j - chunkViewDistance);
//
//                final Chunk chunk = getChunkWithPoint(offChunk);
//                if (chunk == null) {
//                    continue;
//                }
//
//                final float[] grid = chunk.getIdGrid();
//                for (int y = 0; y < chunk.getHeight(); y++) {
//                    for (int z = 0; z < chunk.getDepth(); z++) {
//                        final int gridIdx = y * mapChunkSize + z * mapChunkSize * mapChunkSize;
//                        final int listIdx = (i * mapChunkSize) + (y * worldScale.x) + (z * worldScale.x * worldScale.y + (j * mapChunkSize * worldScale.x * worldScale.y));
//                        System.arraycopy(grid, gridIdx, byteList, listIdx, chunk.getWidth());
//                    }
//                }
//            }
//        }
//
//        worldBuffer = byteList;
    }

    public static boolean outBounds(final int x, final int y, final int z) {
        return (y < 0 || y >= mapChunkSize);
    }

    public static Point getChunkPosition(final int x, final int z) {
        final float w = mapChunkSize;
        return new Point((int) Math.floor(x / w), (int) Math.floor(z / w));
    }

    public Chunk getChunkWithId(final String id) {
        return chunksById.get(id);
    }

    public Chunk getChunkWithPoint(final Point chunkPos) {
        final String id = chunkPos.x + "/" + chunkPos.y;
        return chunksById.get(id);
    }

    public Chunk getChunk(final int x, final int z) {
        final Point chunkPos = World.getChunkPosition(x, z);
        final String id = chunkPos.x + "/" + chunkPos.y;
        return getChunkWithId(id);
    }

    public Chunk getChunkOrCreate(final int x, final int z) {
        final float w = mapChunkSize;
        final Point chunkPos = World.getChunkPosition(x, z);
        final String id = chunkPos.x + "/" + chunkPos.y;
        if (chunksById.containsKey(id)) {
            return getChunk(x, z);
        }

        final Chunk chunk = new Chunk(chunkPos.x * (int) w, chunkPos.y * (int) w, id);
        chunksById.put(id, chunk);
        chunkList.add(chunk);

        return chunk;
    }

    public static Chunk getChunkAtTile(final int x, final int y, final int z, final boolean createIfNull) {
        if (World.outBounds(x, y, z)) {
            return null;
        }

        if (!createIfNull) {
            return world.getChunk(x, z);
        }

        return world.getChunkOrCreate(x, z);
    }

    public static Chunk getChunkAtTile(final Point3D p, final boolean createIfNull) {
        return World.getChunkAtTile(p.x, p.y, p.z, createIfNull);
    }

    public static Element getElement(final int x, final int y, final int z) {
        if (World.outBounds(x, y, z)) {
            return null;
        }

        final Chunk chunk = World.getChunkAtTile(x, y, z, false);

        return chunk != null ? chunk.getElement(x, y, z) : null;
    }

    public static Element getElement(final Point3D p) {
        return World.getElement(p.x, p.y, p.z);
    }

    public static void setElement(final int x, final int y, final int z, final Element e) {
        final Chunk offChunk = World.getChunkAtTile(x, y, z, true);
        if (offChunk == null || offChunk.getElement(x, y, z) != null) {
            return;
        }

        offChunk.setElement(x, y, z, e);
        offChunk.awakeGrid(x, y, z);
    }

    public static void setElement(final Point3D p, final Element e) {
        World.setElement(p.x, p.y, p.z, e);
    }

    public List<Chunk> getChunkList() {
        return chunkList;
    }

    public List<Chunk> getChunkUpdateList() {
        return chunkUpdateList;
    }

    public float[] getWorldBuffer() {
        return worldBuffer;
    }

    public Point3D getWorldScale() {
        return worldScale;
    }

    public Point3D getFractalScale() {
        return fractalScale;
    }

    public Point3D getBufferScale() {
        return bufferScale;
    }
}
