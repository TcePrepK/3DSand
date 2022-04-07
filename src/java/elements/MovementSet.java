package elements;

import game.Chunk;
import toolbox.Points.Point3D;

import java.util.ArrayList;
import java.util.List;

import static core.GlobalVariables.chunkManager;

public class MovementSet {
    private final List<Movement> movementList = new ArrayList<>();
    private final FillerRegistry fillerRegistry;

    public MovementSet(final FillerRegistry fillerRegistry) {
        this.fillerRegistry = fillerRegistry;
    }

    public boolean checkPosition(final Point3D pos) {
        for (final Movement movement : movementList) {
            final Point3D movedPos = pos.add(movement.getOffset());
            final Chunk targetChunk = chunkManager.getChunkWorldSpace(movedPos, true);
            if (targetChunk == null) {
                return false;
            }

            if (!movement.checkInput(targetChunk, movedPos)) {
                return false;
            }
        }

        return true;
    }

    public void setPosition(final Point3D pos) {
        for (final Movement movement : movementList) {
            final Point3D movedPos = pos.add(movement.getOffset());
            final Chunk targetChunk = chunkManager.getChunkWorldSpace(movedPos, true);

            movement.setOutput(targetChunk, movedPos);
        }
    }

    public void addMovement(final Point3D offset, final String input, final String output) {
        movementList.add(new Movement(fillerRegistry, offset, input, output));
    }

    public boolean isEmpty() {
        return movementList.isEmpty();
    }

    @Override
    public String toString() {
        return movementList.toString();
    }
}
