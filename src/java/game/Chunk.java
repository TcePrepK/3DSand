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
import static elements.ElementRegistry.*;
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
    private int totalElementAmount = 0;

    private final ByteBuffer voxelBuffer = BufferUtils.createByteBuffer(chunkScale.x * chunkScale.y * chunkScale.z);
    private final ImageBuffer3D voxelImageBuffer = new ImageBuffer3D(chunkScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);

    private final ByteBuffer bitmaskBuffer = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);
    private final ImageBuffer3D bitmaskImageBuffer = new ImageBuffer3D(bitmaskScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
    private boolean updateBuffers = true;

    public Chunk(final int x, final int y, final int z, final String id) {
        pos = new Point3D(x, y, z).mult(mapChunkSize);
        this.id = id;

        dirtyCube = new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize);
        nextDirtyCube = new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize);

//        generateUpdateTester();
//        generateTerrain(100);
//        generateCaves(100);
//        generateSponge();
//        generateSpongeOnPlatform();
        generateFloatingSpongeInCube();

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

                    setElement(finalX, finalY, finalZ, emptyElement);
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
                        final Vector3D location = normPos.mult(power);
                        voxel = location.mod(3).sub(1).toPoint3D();
                    }

                    if (hit) {
                        setElement(pos.x + offX, pos.y + offY, pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generatePlatform() {
        if (pos.y > 0) {
            return;
        }

        for (int offX = 0; offX < mapChunkSize; offX++) {
            for (int offZ = 0; offZ < mapChunkSize; offZ++) {
                if (rand.nextFloat() < 0.01) {
                    setElement(pos.x + offX, pos.y, pos.z + offZ, ElementRegistry.getElementByName("Light"));
                    continue;
                }

                setElement(pos.x + offX, pos.y, pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
            }
        }
    }

    public void generateSpongeOnPlatform() {
        generatePlatform();

        final float scale = 0.5f;
        final Vector3D worldScale = world.getWorldScale().toVector3D();
        final Vector3D newSize = worldScale.mult(scale);
        final Vector3D center = worldScale.div(2).sub(newSize.div(2)).mult(1, 0, 1).add(0, 2, 0);

        for (int offX = 0; offX < mapChunkSize; offX++) {
            for (int offY = 0; offY < mapChunkSize; offY++) {
                for (int offZ = 0; offZ < mapChunkSize; offZ++) {
                    final Vector3D offset = new Vector3D(offX, offY, offZ).add(pos);

                    final Vector3D scaledOffset = offset.sub(center).div(scale);
                    final Vector3D normPos = scaledOffset.div(worldScale);

                    Point3D voxel = normPos.mult(3).sub(1).toPoint3D();
                    Point3D absVoxel = voxel.abs();
                    if (absVoxel.x > 1 || absVoxel.y > 1 || absVoxel.z > 1) {
                        continue;
                    }

                    int iter = 0;
                    boolean hit = true;
                    while (iter <= 5) {
                        absVoxel = voxel.abs();
                        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
                            hit = false;
                            break;
                        }

                        iter++;

                        final float power = (float) Math.pow(3, iter);
                        final Vector3D location = normPos.mult(power);
                        voxel = location.mod(3).sub(1).toPoint3D();
                    }

                    if (hit) {
                        setElement(pos.x + offX, pos.y + offY, pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generateCube() {
        final Point3D worldScale = world.getWorldScale();
        final float lastChunkPosition = worldScale.x - mapChunkSize;
        if (pos.x != 0 && pos.y != 0 && pos.z != 0 && pos.x != lastChunkPosition && pos.y != lastChunkPosition && pos.z != lastChunkPosition) {
            return;
        }

        for (int i = 0; i < mapChunkSize; i++) {
            for (int j = 0; j < mapChunkSize; j++) {
                final Element element = rand.nextFloat() < 1 ? getElementByName("Light") : getElementByName("Dirt");
                if (pos.x == 0) {
                    setElement(pos.x, pos.y + i, pos.z + j, element);
                } else if (pos.x == lastChunkPosition) {
                    setElement(worldScale.x - 1, pos.y + i, pos.z + j, element);
                }

                if (pos.y == 0) {
                    setElement(pos.x + i, pos.y, pos.z + j, element);
                } else if (pos.y == lastChunkPosition) {
                    setElement(pos.x + i, worldScale.y - 1, pos.z + j, element);
                }

                if (pos.z == 0) {
                    setElement(pos.x + i, pos.y + j, pos.z, element);
                } else if (pos.z == lastChunkPosition) {
                    setElement(pos.x + i, pos.y + j, worldScale.z - 1, element);
                }
            }
        }
    }

    public void generateFloatingSpongeInCube() {
        generateCube();

        final float scale = 0.25f;
        final Vector3D worldScale = world.getWorldScale().toVector3D();
        final Vector3D newSize = worldScale.mult(scale);
        final Vector3D center = worldScale.div(2).sub(newSize.div(2));

        for (int offX = 0; offX < mapChunkSize; offX++) {
            for (int offY = 0; offY < mapChunkSize; offY++) {
                for (int offZ = 0; offZ < mapChunkSize; offZ++) {
                    final Vector3D offset = new Vector3D(offX, offY, offZ).add(pos);

                    final Vector3D scaledOffset = offset.sub(center).div(scale);
                    final Vector3D normPos = scaledOffset.div(worldScale);

                    Point3D voxel = normPos.mult(3).sub(1).toPoint3D();
                    Point3D absVoxel = voxel.abs();
                    if (absVoxel.x > 1 || absVoxel.y > 1 || absVoxel.z > 1) {
                        continue;
                    }

                    int iter = 0;
                    boolean hit = true;
                    while (iter <= 5) {
                        absVoxel = voxel.abs();
                        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
                            hit = false;
                            break;
                        }

                        iter++;

                        final float power = (float) Math.pow(3, iter);
                        final Vector3D location = normPos.mult(power);
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

        updateBuffers = false;

        glDeleteTextures(voxelImageBuffer.getID());
        glDeleteTextures(bitmaskImageBuffer.getID());

        if (isEmpty()) {
            return;
        }

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
    }

    public void awakeGrid(final int x, final int y, final int z) {
        nextDirtyCube = nextDirtyCube.includeCube(new Cube(x, y, z, 1, 1, 1).extend(1));
    }

    public void updateRect() {
//        System.out.println(nextDirtyCube.sub(pos.x, pos.y, pos.z).div(mapChunkSize).ceil());
        dirtyCube = nextDirtyCube.clamp(new Cube(pos.x, pos.y, pos.z, mapChunkSize, mapChunkSize, mapChunkSize));
        nextDirtyCube = new Cube();
    }

    public void setElement(final int x, final int y, final int z, final Element element) {
        if (outBounds(x, y, z)) {
            return;
        }

        awakeGrid(x, y, z);

        final int idx = getIDX(x - pos.x, y - pos.y, z - pos.z);
        final Element previousElement = getElementByID(idGrid[idx]);
        if (element.equals(previousElement)) {
            return;
        }

        grid[idx] = element;
        idGrid[idx] = (byte) element.getId();
        voxelBuffer.put(idx, (byte) element.getId());
        updateBuffers = true;

        if (!element.equals(emptyElement) && !previousElement.equals(emptyElement)) {
            return;
        }

        final int counterDelta = element.equals(emptyElement) ? -1 : 1;
        final int bitmaskIDX = getBitmaskIDX(x - pos.x, y - pos.y, z - pos.z);

        totalElementAmount += counterDelta;
        bitmaskBuffer.put(bitmaskIDX, (byte) (bitmaskBuffer.get(bitmaskIDX) + counterDelta));

    }

    public void setElement(final Point3D pos, final Element e) {
        setElement(pos.x, pos.y, pos.z, e);
    }

    @NotNull
    public Element getElement(final int x, final int y, final int z) {
        if (outBounds(x, y, z)) {
            return emptyElement;
        }

        final byte id = idGrid[getIDX(x - pos.x, y - pos.y, z - pos.z)];
        return ElementRegistry.getElementByID(id);
    }

    @NotNull
    public Element getElement(final Point3D pos) {
        return getElement(pos.x, pos.y, pos.z);
    }

    public boolean isEmpty() {
        return totalElementAmount == 0;
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

    public boolean outBounds(final int x, final int y, final int z) {
        return (x < pos.x || x >= pos.x + chunkScale.x || y < pos.y || y >= pos.y + chunkScale.y || z < pos.z || z >= pos.z + chunkScale.z);
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
    
