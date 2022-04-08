package game;

import toolbox.Points.Point3D;

import java.util.ArrayList;
import java.util.List;

import static core.GlobalVariables.*;

public class World {
    private final List<Point3D> chunkGenerationList = new ArrayList<>();

    private final Point3D worldScale = new Point3D(2 * chunkViewDistance * mapChunkSize);

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
