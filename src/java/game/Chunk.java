package game;

import com.sun.istack.internal.NotNull;
import core.imageBuffers.ImageBuffer3D;
import elements.Element;
import elements.ElementRegistry;
import org.lwjgl.BufferUtils;
import toolbox.Cube;
import toolbox.Noise;
import toolbox.Points.Point3D;
import toolbox.Vector3D;

import java.nio.ByteBuffer;

import static core.GlobalVariables.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_R8;

public class Chunk {
    private final Point3D pos;
    private final String id;

    private Cube dirtyCube;
    private Cube nextDirtyCube;

    private final Point3D chunkScale = new Point3D(mapChunkSize);
    private final Point3D bitmaskScale = new Point3D(mapChunkSize / mapBitmaskSize);
    private final Element[] grid = new Element[chunkScale.x * chunkScale.y * chunkScale.z];
    private final byte[] idGrid = new byte[chunkScale.x * chunkScale.y * chunkScale.z];

    private final ByteBuffer voxelBuffer = BufferUtils.createByteBuffer(chunkScale.x * chunkScale.y * chunkScale.z);
    private final ImageBuffer3D voxelImageBuffer = new ImageBuffer3D(chunkScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);

    private final ByteBuffer bitmaskBuffer = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);
    private final ImageBuffer3D bitmaskImageBuffer = new ImageBuffer3D(bitmaskScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
    private boolean updateBuffers = true;

//    private final int bitmaskSize = 4;
//    private final Point3D bitmaskScale = chunkScale.div(bitmaskSize);
//    private final int[] bitmaskGrid = new int[bitmaskScale.x * bitmaskScale.y * bitmaskScale.z];

    public Chunk(final int x, final int y, final int z, final String id) {
        pos = new Point3D(x, y, z).mult(mapChunkSize);
        this.id = id;

        dirtyCube = new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize);
        nextDirtyCube = new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize);

//        generateUpdateTester();
        generateTerrain(100);
        generateCaves(100);
//        generateSponge();

//        generateNoiseChunk();

        updateRect();
    }

    public void generateUpdateTester() {
        final int finalY = pos.y + chunkScale.y - 1;
        for (int offX = 0; offX < chunkScale.x; offX++) {
            final int finalX = pos.x + offX;
            for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                final int finalZ = pos.z + offZ;

                setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Light"));
            }
        }
    }

    public void generateTerrain(final float scale) {
        final float lightScale1 = 100;
        final float lightScale2 = 10;
        final float maxChunkDepth = 3;
        for (int offX = 0; offX < chunkScale.x; offX++) {
            final int finalX = pos.x + offX;
            for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                final int finalZ = pos.z + offZ;

                final float height;
                if (pos.y < (2 * chunkViewDistance - maxChunkDepth) * mapChunkSize) {
                    height = mapChunkSize;
                } else {
                    final float noise = (float) Math.abs(Noise.noise(finalX / scale, finalZ / scale)); // [0, 1]
                    final float targetHeight = noise * mapChunkSize * maxChunkDepth; // [0, 64]
                    final float chunkHeight = pos.y - (2 * chunkViewDistance - maxChunkDepth) * mapChunkSize;
                    height = targetHeight - chunkHeight;
                }

                for (int finalY = pos.y; finalY < pos.y + height; finalY++) {
                    final double noise1 = Noise.noise(finalX / lightScale1, finalY / lightScale1, finalZ / lightScale1) * 0.85;
                    final double noise2 = Noise.noise(finalX / lightScale2, finalY / lightScale2, finalZ / lightScale2) * 0.15;
                    if (noise1 + noise2 < 0.05) {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Light"));
                    } else {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generateCaves(final float scale) {
        for (int offX = 0; offX < chunkScale.x; offX++) {
            final int finalX = pos.x + offX;
            for (int offY = 0; offY < chunkScale.y; offY++) {
                final int finalY = pos.y + offY;
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    final int finalZ = pos.z + offZ;

                    final float val = (float) Math.abs(Noise.noise(finalX / scale, finalY / scale, finalZ / scale));
                    if (val > 0.12) {
                        continue;
                    }

                    final Element element = getElement(finalX, finalY, finalZ);
                    if (element == null) {
                        continue;
                    }

                    setElement(finalX, finalY, finalZ, ElementRegistry.emptyElement);
                }
            }
        }
    }

    public void generateNoiseChunk() {
        final int noiseX = (int) (rand.nextFloat() * 1000);
        final int noiseY = (int) (rand.nextFloat() * 1000);
        final int noiseZ = (int) (rand.nextFloat() * 1000);
        final float scale1 = rand.nextFloat() * mapChunkSize * 2;
        final float scale2 = 50;
        for (int offX = 0; offX < chunkScale.x; offX++) {
            final int finalX = pos.x + offX;
            for (int offY = 0; offY < chunkScale.y; offY++) {
                final int finalY = pos.y + offY;
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    final int finalZ = pos.z + offZ;
                    final float val = (float) Math.abs(Noise.noise(finalX / scale1 + noiseX, finalY / scale1 + noiseY, finalZ / scale1 + noiseZ));

                    if (val < 0.5) {
                        continue;
                    }

                    if (Math.abs(Noise.noise(finalX / scale2, finalY / scale2, finalZ / scale2)) < 0.01) {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Sand"));
                    } else {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generateSponge() {
        for (int offX = 0; offX < chunkScale.x; offX++) {
            for (int offY = 0; offY < chunkScale.y; offY++) {
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    final Vector3D normPos = new Vector3D(pos.x + offX, pos.y + offY, pos.z + offZ).div(world.getWorldScale().toVector3D());

                    int iter = 0;
                    boolean hit = true;
                    Point3D voxel = normPos.mult(3).sub(1).toPoint3D();
                    while (iter <= 4) {
                        final Point3D absVoxel = voxel.abs();
                        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
                            hit = false;
                            break;
                        }

                        iter++;

                        final float power = (float) Math.pow(3, iter);
                        final Vector3D location = normPos.mult(power).floor();
                        voxel = location.mod(3).sub(1).toPoint3D();
                    }

                    if (hit) {
                        setElement(pos.x + offX, pos.y + offY, pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void updateBuffers() {
        if (!updateBuffers) {
            return;
        }

        glDeleteTextures(voxelImageBuffer.getID());
        glDeleteTextures(bitmaskImageBuffer.getID());

        voxelImageBuffer.create(voxelBuffer);
        bitmaskImageBuffer.create(bitmaskBuffer);

//        if (voxelImageBuffer.isEmpty()) {
//            voxelImageBuffer.create(null);
//        }
//
//        if (bitmaskImageBuffer.isEmpty()) {
//            bitmaskImageBuffer.create(null);
//        }
//
//        voxelImageBuffer.updatePixels(voxelBuffer);
//        bitmaskImageBuffer.updatePixels(bitmaskBuffer);

        updateBuffers = false;
    }

    public void awakeGrid(final int x, final int y, final int z) {
        nextDirtyCube = nextDirtyCube.includeCube(new Cube(x, y, z, 1, 1, 1).extend(1));
    }

    public void updateRect() {
        System.out.println(nextDirtyCube.sub(pos.x, pos.y, pos.z).div(mapChunkSize).ceil());
        dirtyCube = nextDirtyCube.clamp(new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize));
        nextDirtyCube = new Cube();
    }

    public void setElement(final int x, final int y, final int z, final Element e) {
        if (outBounds(x, y, z)) {
            return;
        }

        awakeGrid(x, y, z);

        final int idx = getIDX(x - pos.x, y - pos.y, z - pos.z);
        grid[idx] = e;
        idGrid[idx] = (byte) e.getId();
        voxelBuffer.put(idx, (byte) e.getId());

        final int bitmaskIDX = getBitmaskIDX(x - pos.x, y - pos.y, z - pos.z);
        final int previousData = bitmaskBuffer.get(bitmaskIDX);
        bitmaskBuffer.put(bitmaskIDX, (byte) (previousData + ((e.getName() == null) ? -1 : 1)));

        updateBuffers = true;
    }

    public void setElement(final Point3D pos, final Element e) {
        setElement(pos.x, pos.y, pos.z, e);
    }

    @NotNull
    public Element getElement(final int x, final int y, final int z) {
        if (outBounds(x, y, z)) {
            return ElementRegistry.emptyElement;
        }

        final byte id = idGrid[getIDX(x - pos.x, y - pos.y, z - pos.z)];
        return ElementRegistry.getElementByID(id);
    }

    @NotNull
    public Element getElement(final Point3D pos) {
        return getElement(pos.x, pos.y, pos.z);
    }

    public boolean outBounds(final int x, final int y, final int z) {
        return (x < pos.x || x >= pos.x + chunkScale.x || y < pos.y || y >= pos.y + chunkScale.y || z < pos.z || z >= pos.z + chunkScale.z);
    }

    public int getIDX(final int x, final int y, final int z) {
        return x + (y * chunkScale.x) + (z * chunkScale.x * chunkScale.y);
    }

    public int getBitmaskIDX(final int x, final int y, final int z) {
        final int bitX = x / mapBitmaskSize;
        final int bitY = y / mapBitmaskSize;
        final int bitZ = z / mapBitmaskSize;
        return bitX + bitY * bitmaskScale.x + bitZ * bitmaskScale.x * bitmaskScale.y;
    }

    public ImageBuffer3D getVoxelBuffer() {
        return voxelImageBuffer;
    }

    public ImageBuffer3D getBitmaskBuffer() {
        return bitmaskImageBuffer;
    }

    public int getHeight() {
        return chunkScale.y;
    }

    public int getX() {
        return pos.x;
    }

    public int getY() {
        return pos.y;
    }

    public int getZ() {
        return pos.z;
    }

    public String getId() {
        return id;
    }

    public boolean shouldUpdateBuffer() {
        return updateBuffers;
    }
}
    
