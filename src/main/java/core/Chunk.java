package core;

import elements.Element;
import elements.ElementRegistry;
import toolbox.*;

import static core.GlobalVariables.*;

public class Chunk {
    private final Point3D pos;
    private final String id;

    private int minX, maxX, minY, maxY, minZ, maxZ;
    private int minXw, maxXw, minYw, maxYw, minZw, maxZw;

    private final Octatree octaTree;

    private final Point3D chunkScale = new Point3D(mapChunkSize);
    private final Element[] grid = new Element[chunkScale.x * chunkScale.y * chunkScale.z];
    private final float[] idGrid = new float[chunkScale.x * chunkScale.y * chunkScale.z];

//    private final int bitmaskSize = 4;
//    private final Point3D bitmaskScale = chunkScale.div(bitmaskSize);
//    private final int[] bitmaskGrid = new int[bitmaskScale.x * bitmaskScale.y * bitmaskScale.z];

    public Chunk(final int x, final int y, final int z, final String id) {
        pos = new Point3D(x, y, z);
        this.id = id;

        octaTree = new Octatree(new Vector3D(x, y, z), chunkScale.x, 16);

        // Generate
        if (noisyWorld) {
            generateChunkViaNoise();
        } else {
            generateSponge();
//            generateChunk();
        }
    }

    public void generateChunkViaNoise() {
        for (int offX = 0; offX < chunkScale.x; offX++) {
            for (int offY = 0; offY < chunkScale.y; offY++) {
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    final float scale = 100;
                    final int finalX = pos.x + offX;
                    final int finalY = pos.y + offY;
                    final int finalZ = pos.z + offZ;
                    final float noise = (float) Math.abs(Noise.noise(finalX / scale, finalY / scale, finalZ / scale));

                    if (noise >= 0.5) {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generateChunk() {
        for (int offX = 0; offX < chunkScale.x; offX++) {
            for (int offY = 0; offY < chunkScale.y; offY++) {
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    setElement(pos.x + offX, pos.y + offY, pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
                }
            }
        }
    }

    public void generateSponge() {
        for (int offX = 0; offX < chunkScale.x; offX++) {
            for (int offY = 0; offY < chunkScale.y; offY++) {
                for (int offZ = 0; offZ < chunkScale.z; offZ++) {
                    final Point3D fin = new Point3D(pos.x + offX, pos.y + offY, pos.z + offZ).add(world.getWorldScale().div(2));
                    final int finalX = fin.x;
                    final int finalY = fin.y;
                    final int finalZ = fin.z;
                    final Vector3D pos = new Vector3D(finalX, finalY, finalZ).div(world.getWorldScale().toVector3D());

                    int iter = 0;
                    boolean hit = true;
                    Point3D voxel = pos.mult(3).sub(1).floor().toPoint3D();
                    while (iter <= 4) {
                        final Point3D absVoxel = voxel.abs();
                        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
                            hit = false;
                            break;
                        }

                        iter++;

                        final float power = (float) Math.pow(3, iter);
                        final Vector3D location = pos.mult(power).floor();
                        voxel = location.mod(3).sub(1).toPoint3D();
                    }

                    if (hit) {
                        setElement(this.pos.x + offX, this.pos.y + offY, this.pos.z + offZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }


    public void awakeGrid(final int x, final int y, final int z) {
        minXw = Math.min(minXw, x - 1);
        minYw = Math.min(minYw, y - 1);
        minZw = Math.min(minZw, z - 1);

        maxXw = Math.max(maxXw, x + 2);
        maxYw = Math.max(maxYw, y + 2);
        maxZw = Math.max(maxZw, z + 2);

        if (world.getChunkUpdateList().contains(this)) {
            return;
        }

        world.getChunkUpdateList().add(this);
    }

    public void awakeGrid(final Point3D pos) {
        awakeGrid(pos.x, pos.y, pos.z);
    }

    public void updateRect(final boolean updatedThisFrame) {
        minX = (int) Maths.clamp(minXw, pos.x, pos.x + chunkScale.x);
        minY = (int) Maths.clamp(minYw, 0, chunkScale.y);
        minZ = (int) Maths.clamp(minZw, pos.z, pos.z + chunkScale.z);
        maxX = (int) Maths.clamp(maxXw, pos.x, pos.x + chunkScale.x);
        maxY = (int) Maths.clamp(maxYw, 0, chunkScale.y);
        maxZ = (int) Maths.clamp(maxZw, pos.z, pos.z + chunkScale.z);

        if (updatedThisFrame) {
            minXw = pos.x + chunkScale.x;
            minYw = chunkScale.y;
            minZw = pos.z + chunkScale.z;
            maxXw = pos.x;
            maxYw = 0;
            maxZw = pos.z;
        } else {
            minXw = 0;
            minYw = 0;
            minZw = 0;
            maxXw = 0;
            maxYw = 0;
            maxZw = 0;
        }
    }

    public int getMinHeight(final int x, final int z) {
        int currentHeight = 0;
        while (currentHeight < chunkScale.y) {
            final Element e = getElement(x, currentHeight, z);
            if (e == null) {
                return currentHeight;
            }

            currentHeight++;
        }

        return currentHeight;
    }

    public void setElement(final int x, final int y, final int z, final Element e) {
        if (outBounds(x, y, z)) {
            return;
        }

        final int idx = getIdx(x - pos.x, y - pos.y, z - pos.z);
        grid[idx] = e;
        idGrid[idx] = e == null ? 0 : e.getId();
        world.setBufferElement(x, y, z, e);
        octaTree.addPoint(new Point3D(x, y, z));
//        bitmaskGrid[getBitmaskIdx(x - pos.x, y - pos.y, z - pos.z)] += e == null ? -1 : 1;
    }

    public void setElement(final Point3D pos, final Element e) {
        setElement(pos.x, pos.y, pos.z, e);
    }

    public Element getElement(final int x, final int y, final int z) {
        if (outBounds(x, y, z)) {
            return null;
        }

        return grid[getIdx(x - pos.x, y - pos.y, z - pos.z)];
    }

    public Element getElement(final Point3D pos) {
        return getElement(pos.x, pos.y, pos.z);
    }

    public boolean outBounds(final int x, final int y, final int z) {
        return (x < pos.x || x >= pos.x + chunkScale.x || y < pos.y || y >= pos.y + chunkScale.y || z < pos.z || z >= pos.z + chunkScale.z);
    }

    public int getIdx(final int x, final int y, final int z) {
        return x + (y * chunkScale.x) + (z * chunkScale.x * chunkScale.y);
    }

//    public int getBitmaskIdx(final int x, final int y, final int z) {
//        return x / bitmaskSize + (y / bitmaskSize * bitmaskScale.x) + (z / bitmaskSize * bitmaskScale.x * bitmaskScale.y);
//    }

    public int getHeight() {
        return chunkScale.y;
    }

    public int getX() {
        return pos.x;
    }

    public int getZ() {
        return pos.z;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getId() {
        return id;
    }

    public Octatree getOctaTree() {
        return octaTree;
    }

//    public int[] getBitmaskGrid() {
//        return bitmaskGrid;
//    }
}
    
