package game;

import elements.Element;
import org.lwjgl.BufferUtils;
import toolbox.Points.Point3D;
import toolbox.Timer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.GlobalVariables.*;

public class World {
    private final Map<String, Chunk> chunksById = new HashMap<>();
    private final List<Chunk> chunkList = new ArrayList<>();
    private final List<Chunk> chunkUpdateList = new ArrayList<>();

    //    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize, mapChunkSize, 2 * chunkViewDistance * mapChunkSize);
    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize);

    public final ByteBuffer worldBuffer = BufferUtils.createByteBuffer(worldScale.x * worldScale.y * worldScale.z);
    private final Point3D bufferScale = worldScale;

    private final int bitmaskSize = 4;
    private final Point3D bitmaskScale = worldScale.div(bitmaskSize);
    private final int[] bitmaskSizeGrid = new int[bitmaskScale.x * bitmaskScale.y * bitmaskScale.z];
    private final ByteBuffer bitmaskGrid = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);

    public final List<Point3D> chunkGenerationList = new ArrayList<>();
    private double totalGenerationTime;

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
            world.getChunkByChunkPos(selectedPos, true);
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
        final int oX = x + worldScale.x / 2;
        final int oY = y + worldScale.y / 2;
        final int oZ = z + worldScale.z / 2;

        if (oX < 0 || oY < 0 || oZ < 0 || oX >= worldScale.x || oY >= worldScale.y || oZ >= worldScale.z) {
//            System.out.println("Error! Tried to place element to: " + new Point3D(oX, oY, oZ));
            return;
        }

        final int worldIDX = getBufferIDX(oX, oY, oZ);
        final byte replacedID = worldBuffer.get(worldIDX);
        final byte newID = e == null ? 0 : (byte) e.getId();
        worldBuffer.put(worldIDX, newID);

        if (replacedID == newID) {
            return;
        }

        final int bitmaskIDX = getBitmaskIdx(oX, oY, oZ);
        bitmaskSizeGrid[bitmaskIDX] += newID == 0 ? -1 : 1;
        bitmaskGrid.put(bitmaskIDX, (byte) ((bitmaskSizeGrid[bitmaskIDX] == 0) ? 0 : 1));

        renderer.recreateWorldTexture = true;
    }

    public void updateBuffer() {
//        final Point3D cameraPos = camera.getPosition().floor().toPoint3D();
//        final Point startingChunk = World.getChunkPosition(cameraPos.x, cameraPos.z);
//        final Point startingChunk = new Point(0, 0);
//
//        final int totalLength = worldScale.x * worldScale.y * worldScale.z;
//        final float[] byteList = new float[totalLength];
//
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

    public static Point3D getChunkPosition(final int x, final int y, final int z) {
        final float w = mapChunkSize;
        return new Point3D((int) Math.floor(x / w), (int) Math.floor(y / w), (int) Math.floor(z / w));
    }

    public Chunk getChunkWithId(final String id) {
        return chunksById.get(id);
    }

    public Chunk getChunkWithPoint(final Point3D chunkPos) {
        return chunksById.get(chunkPos.toString());
    }

    public Chunk getChunk(final int x, final int y, final int z) {
        final Point3D chunkPos = World.getChunkPosition(x, y, z);
        return getChunkWithId(chunkPos.toString());
    }

    public Chunk getChunkOrCreate(final int x, final int y, final int z) {
        final int w = mapChunkSize;
        final Point3D chunkPos = World.getChunkPosition(x, y, z);
        final String id = chunkPos.toString();
        if (chunksById.containsKey(id)) {
            return getChunkWithId(id);
        }

        final Chunk chunk = new Chunk(chunkPos.x * w, chunkPos.y * w, chunkPos.z * w, id);
        chunksById.put(id, chunk);
        chunkList.add(chunk);

        return chunk;
    }

    public Chunk getChunkByChunkPos(final Point3D chunkPos, final boolean createIfNull) {
        final String id = chunkPos.toString();
        if (chunksById.containsKey(id)) {
            return getChunkWithId(id);
        }

        if (!createIfNull) {
            return null;
        }

        final int w = mapChunkSize;
        final Chunk chunk = new Chunk(chunkPos.x * w, chunkPos.y * w, chunkPos.z * w, id);
        chunksById.put(id, chunk);
        chunkList.add(chunk);

        return chunk;
    }

    public static Chunk getChunkAtTile(final int x, final int y, final int z, final boolean createIfNull) {
//        if (World.outBounds(x, y, z)) {
//            return null;
//        }

        if (!createIfNull) {
            return world.getChunk(x, y, z);
        }

        return world.getChunkOrCreate(x, y, z);
    }

    public static Chunk getChunkAtTile(final Point3D p, final boolean createIfNull) {
        return World.getChunkAtTile(p.x, p.y, p.z, createIfNull);
    }

    public static Element getElement(final int x, final int y, final int z) {
//        if (World.outBounds(x, y, z)) {
//            return null;
//        }

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

    public ByteBuffer getWorldBuffer() {
        return worldBuffer;
    }

    public Point3D getWorldScale() {
        return worldScale;
    }

    public Point3D getBufferScale() {
        return bufferScale;
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
