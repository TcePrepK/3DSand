package elements;

import core.Chunk;
import core.World;
import models.ColoredModel;
import models.PlaneModelCreator;
import models.RawModel;
import org.joml.Vector3f;
import toolbox.Color;
import toolbox.Mouse;
import toolbox.Points.Point3D;

import static core.GlobalVariables.*;

public class ElementPlacer {
    private Point3D currentPosition;
    private ColoredModel plane;

    public void init() {
        final RawModel rawModel = PlaneModelCreator.getPlaneRawModel(1, 1, new Color(1, 0, 0, 0.75f));
        plane = new ColoredModel(0, 1, 0, rawModel);

        Mouse.mouseLeftDown.add(this::mouseLeftDown);
    }

    public void update() {
        mousePicker.update();

        final Vector3f pos = mousePicker.getCurrentWorldPoint();
        if (pos == null) {
            currentPosition = null;
            plane.setPosition(0, -0.01f, 0);
            return;
        }

        currentPosition = new Point3D((int) Math.floor(pos.x()), 0, (int) Math.floor(pos.z()));
        final Chunk chunk = World.getChunkAtTile(currentPosition, false);

        int currentHeight = 0;
        if (chunk != null) {
            while (currentHeight < chunk.getHeight()) {
                final Element e = chunk.getElement(currentPosition.x, currentHeight, currentPosition.z);
                if (e == null) {
                    break;
                }

                currentHeight++;
            }
        }

        plane.setPosition(currentPosition.x + 0.5f, currentHeight + 0.01f, currentPosition.z + 0.5f);
    }

    private void mouseLeftDown() {
        if (currentPosition == null) {
            return;
        }


        if (currentFrame % 12 != 0) {
            // return;
        }

        final Chunk chunk = World.getChunkAtTile(currentPosition.x, 0, currentPosition.z, true);
        if (chunk == null) {
            return;
        }

        final int x = currentPosition.x;
        final int z = currentPosition.z;
        int height = mapChunkSize - 1;
        while (height > 0) {
            final Element e = chunk.getElement(currentPosition.x, height, currentPosition.z);
            if (e == null) {
                break;
            }

            height--;
        }

        final int w = 4;
        for (int i = -(w - (w + 1) / 2); i <= w - (w + 1) / 2; i++) {
            for (int j = -(w - (w + 1) / 2); j <= w - (w + 1) / 2; j++) {
                if (rand.nextFloat() < 0.2f) {
                    continue;
                }

                final Point3D tile = new Point3D(x + i, height, z + j);
                final Element e = ElementRegistry.getElementByName(currentMat);

                World.setElement(tile, e);
            }
        }
    }
}
