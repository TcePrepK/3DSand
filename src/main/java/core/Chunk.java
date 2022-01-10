package core;

import elements.Element;
import elements.ElementRegistry;
import toolbox.*;

import static core.GlobalVariables.*;

public class Chunk {
    private final int w, h, d;
    private final int x, y, z;
    private final String id;

    private int minX, maxX, minY, maxY, minZ, maxZ;
    private int minXw, maxXw, minYw, maxYw, minZw, maxZw;

    private final Octatree octaTree;
    private final Element[] grid;
    private final float[] idGrid;

    public Chunk(final int x, final int y, final int z, final String id) {
        w = h = d = mapChunkSize;

        this.x = x;
        this.y = y;
        this.z = z;

        this.id = id;

        octaTree = new Octatree(new Vector3D(x, y, z), w, 16);
        grid = new Element[w * h * d];
        idGrid = new float[w * h * d];

        // Generate
        if (noisyWorld) {
            generateChunkViaNoise();
        } else {
            generateSponge();
//            generateChunk();
        }
    }

    public void generateChunkViaNoise() {
        for (int offX = 0; offX < w; offX++) {
            for (int offY = 0; offY < h; offY++) {
                for (int offZ = 0; offZ < d; offZ++) {
                    final float scale = 100;
                    final int finalX = x + offX;
                    final int finalY = y + offY;
                    final int finalZ = z + offZ;
                    final float noise = (float) Math.abs(Noise.noise(finalX / scale, finalY / scale, finalZ / scale));

                    if (noise >= 0.5) {
                        setElement(finalX, finalY, finalZ, ElementRegistry.getElementByName("Dirt"));
                    }
                }
            }
        }
    }

    public void generateChunk() {
        for (int offX = 0; offX < w; offX++) {
            for (int offY = 0; offY < h; offY++) {
                for (int offZ = 0; offZ < d; offZ++) {
                    setElement(x + offX, y + offY, z + offZ, ElementRegistry.getElementByName("Dirt"));
                }
            }
        }
    }

    public void generateSponge() {
        for (int offX = 0; offX < w; offX++) {
            for (int offY = 0; offY < h; offY++) {
                for (int offZ = 0; offZ < d; offZ++) {
                    final Point3D fin = new Point3D(x + offX, y + offY, z + offZ).add(world.getWorldScale().div(2));
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
                        setElement(x + offX, y + offY, z + offZ, ElementRegistry.getElementByName("Dirt"));
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
        minX = (int) Maths.clamp(minXw, x, x + w);
        minY = (int) Maths.clamp(minYw, 0, h);
        minZ = (int) Maths.clamp(minZw, z, z + d);
        maxX = (int) Maths.clamp(maxXw, x, x + w);
        maxY = (int) Maths.clamp(maxYw, 0, h);
        maxZ = (int) Maths.clamp(maxZw, z, z + d);

        if (updatedThisFrame) {
            minXw = x + w;
            minYw = h;
            minZw = z + d;
            maxXw = x;
            maxYw = 0;
            maxZw = z;
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
        while (currentHeight < h) {
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

        final int idx = getIdx(x - this.x, y - this.y, z - this.z);
        grid[idx] = e;
        idGrid[idx] = e == null ? 0 : e.getId();
        world.setBufferElement(x, y, z, e);
        octaTree.addPoint(new Point3D(x, y, z));
    }

    public void setElement(final Point3D pos, final Element e) {
        setElement(pos.x, pos.y, pos.z, e);
    }

    public Element getElement(final int x, final int y, final int z) {
        if (outBounds(x, y, z)) {
            return null;
        }

        return grid[getIdx(x - this.x, y - this.y, z - this.z)];
    }

    public Element getElement(final Point3D pos) {
        return getElement(pos.x, pos.y, pos.z);
    }

    public boolean outBounds(final int x, final int y, final int z) {
        return (x < this.x || x >= this.x + w || y < this.y || y >= this.y + h || z < this.z || z >= this.z + d);
    }

    public int getIdx(final int x, final int y, final int z) {
        return x + (y * w) + (z * w * d);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getDepth() {
        return d;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
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

    public Element[] getGrid() {
        return grid;
    }

    public float[] getIdGrid() {
        return idGrid;
    }
}
    
