package core;

import elements.Element;
import elements.ElementRegistry;
import toolbox.Maths;
import toolbox.Noise;
import toolbox.Point3D;

import static core.GlobalVariables.*;

public class Chunk {
    private final int w, h, d;
    private final int x, z;
    private final String id;

    private int minX, maxX, minY, maxY, minZ, maxZ;
    private int minXw, maxXw, minYw, maxYw, minZw, maxZw;

    private final Element[] grid;
    private final byte[] idGrid;

    public Chunk(final int x, final int z, final String id) {
        w = h = d = mapChunkSize;

        this.x = x;
        this.z = z;

        this.id = id;
        grid = new Element[w * h * d];
        idGrid = new byte[w * h * d];

        // Generate
        if (noisyWorld) {
            generateChunkViaNoise();
        } else {
            generateChunk();
        }
    }

    public void generateChunkViaNoise() {
        for (int offX = 0; offX < w; offX++) {
            for (int offZ = 0; offZ < d; offZ++) {
                final float scale = 100;
                final int finalX = x + offX;
                final int finalZ = z + offZ;
                int height = (int) ((Noise.noise(finalX / scale, finalZ / scale) + 1) * h / 2);

                while (height >= 0) {
                    setElement(x + offX, height--, z + offZ, ElementRegistry.getElementByName("Dirt"));
                }
            }
        }
    }

    public void generateChunk() {
        for (int offX = 0; offX < w; offX++) {
            for (int offZ = 0; offZ < d; offZ++) {
                setElement(x + offX, 0, z + offZ, ElementRegistry.getElementByName("Dirt"));
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

        grid[getIdx(x - this.x, y, z - this.z)] = e;
        idGrid[getIdx(x - this.x, y, z - this.z)] = e == null ? 0 : (byte) e.getId();
    }

    public void setElement(final Point3D pos, final Element e) {
        setElement(pos.x, pos.y, pos.z, e);
    }

    public Element getElement(final int x, final int y, final int z) {
        if (outBounds(x, y, z)) {
            return null;
        }

        return grid[getIdx(x - this.x, y, z - this.z)];
    }

    public Element getElement(final Point3D pos) {
        return getElement(pos.x, pos.y, pos.z);
    }

    public boolean outBounds(final int x, final int y, final int z) {
        return (x < this.x || x >= this.x + w || y < 0 || y >= h || z < this.z || z >= this.z + d);
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

    public Element[] getGrid() {
        return grid;
    }

    public byte[] getIdGrid() {
        return idGrid;
    }
}
    
