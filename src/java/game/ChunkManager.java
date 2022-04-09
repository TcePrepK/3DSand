package game;

import elements.Element;
import org.lwjgl.opengl.ARBBindlessTexture;
import toolbox.Points.Point3D;
import toolbox.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.GlobalVariables.mapChunkSize;

public class ChunkManager {
    public static int OUT_OF_BOUNDS = -1;

    public final int WIDTH, HEIGHT, DEPTH;
    public final int CHUNK_AMOUNT;

    private final Map<String, Chunk> chunksById = new HashMap<>();
    private final List<Chunk> chunkList = new ArrayList<>();
    private final List<Chunk> chunkUpdateList = new ArrayList<>();

    private final Chunk[] chunkArray;

    private final long[] voxelBufferIDArray;
    private final long[] bitmaskBufferIDArray;

    public ChunkManager(final int width, final int height, final int depth) {
        WIDTH = width;
        HEIGHT = height;
        DEPTH = depth;
        CHUNK_AMOUNT = WIDTH * HEIGHT * DEPTH;

        chunkArray = new Chunk[width * height * depth];

        voxelBufferIDArray = new long[width * height * depth];
        bitmaskBufferIDArray = new long[width * height * depth];
    }

    public void updateBuffers() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    final int idx = getIDX(x, y, z);
                    final Chunk chunk = chunkArray[idx];
                    if (chunk == null) {
                        continue;
                    }

                    if (!chunk.shouldUpdateBuffer()) {
                        continue;
                    }
                    
                    chunk.updateBuffers();

                    voxelBufferIDArray[idx] = ARBBindlessTexture.glGetTextureHandleARB(chunk.getVoxelBuffer().getID());
                    bitmaskBufferIDArray[idx] = ARBBindlessTexture.glGetTextureHandleARB(chunk.getBitmaskBuffer().getID());

                    ARBBindlessTexture.glMakeTextureHandleResidentARB(voxelBufferIDArray[idx]);
                    ARBBindlessTexture.glMakeTextureHandleResidentARB(bitmaskBufferIDArray[idx]);
                }
            }
        }
    }

    public void cleanUp() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    final int idx = getIDX(x, y, z);
                    final Chunk chunk = chunkArray[idx];
                    if (chunk == null) {
                        continue;
                    }

                    chunk.getVoxelBuffer().delete();
                }
            }
        }
    }

    public Chunk createChunkChunkSpace(final Point3D chunkPos) {
        final String id = chunkPos.toString();
        final Chunk chunk = new Chunk(chunkPos.x, chunkPos.y, chunkPos.z, id);
        chunksById.put(id, chunk);
        chunkList.add(chunk);

        final int idx = getIDX(chunkPos);
        if (idx != ChunkManager.OUT_OF_BOUNDS) {
            chunkArray[idx] = chunk;
            chunkUpdateList.add(chunk);
        }

        return chunk;
    }

    public static Point3D getChunkPosition(final int x, final int y, final int z) {
        return new Vector3D(x, y, z).div(mapChunkSize).toPoint3D();
    }

    public Chunk getChunkWithId(final String id) {
        return chunksById.get(id);
    }

    public Chunk getChunkWorldSpace(final Point3D pos, final boolean createIfNull) {
        return getChunkWorldSpace(pos.x, pos.y, pos.z, createIfNull);
    }

    public Chunk getChunkWorldSpace(final int x, final int y, final int z, final boolean createIfNull) {
        final Point3D chunkPos = ChunkManager.getChunkPosition(x, y, z);
        final Chunk chunk = getChunkWithId(chunkPos.toString());

        if (chunk != null) {
            return chunk;
        }

        if (!createIfNull) {
            return null;
        }

        return createChunkChunkSpace(chunkPos);
    }

    public Chunk getChunkChunkSpace(final int x, final int y, final int z, final boolean createIfNull) {
        final Point3D chunkPos = new Point3D(x, y, z);
        final Chunk chunk = getChunkWithId(chunkPos.toString());

        if (chunk != null) {
            return chunk;
        }

        if (!createIfNull) {
            return null;
        }

        return createChunkChunkSpace(chunkPos);
    }

    public Element getElement(final int x, final int y, final int z) {
        final Chunk chunk = getChunkWorldSpace(x, y, z, false);
        return chunk != null ? chunk.getElement(x, y, z) : null;
    }

    public void setElement(final int x, final int y, final int z, final Element e) {
        final Chunk chunk = getChunkWorldSpace(x, y, z, true);
        if (chunk.getElement(x, y, z) != null) {
            return;
        }

        chunk.setElement(x, y, z, e);
        chunk.awakeGrid(x, y, z);
    }

    public boolean inBounds(final int x, final int y, final int z) {
        return !(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH);
    }

    public int getIDX(final int x, final int y, final int z) {
        if (!inBounds(x, y, z)) {
            return ChunkManager.OUT_OF_BOUNDS;
        }

        return x + y * WIDTH + z * WIDTH * HEIGHT;
    }

    public int getIDX(final Point3D pos) {
        return getIDX(pos.x, pos.y, pos.z);
    }

    public long[] getVoxelBufferIDArray() {
        return voxelBufferIDArray;
    }

    public long[] getBitmaskBufferIDArray() {
        return bitmaskBufferIDArray;
    }

    public List<Chunk> getChunkUpdateList() {
        return chunkUpdateList;
    }
}
