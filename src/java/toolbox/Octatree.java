package toolbox;

import toolbox.Points.Point3D;

import java.util.ArrayList;
import java.util.List;

public class Octatree {
    private final float x, y, z, width;
    private final int limit;

    private final List<Point3D> points;
    private final List<Octatree> branches;

    private boolean branched = false;

    public Octatree(final Vector3D pos, final float width, final int limit) {
        x = pos.x;
        y = pos.y;
        z = pos.z;
        this.width = width;
        this.limit = limit;

        points = new ArrayList<>();
        branches = new ArrayList<>();
    }

    private boolean inBounds(final Point3D point) {
        return (point.x >= x && point.x < x + width &&
                point.y >= y && point.y < y + width &&
                point.z >= z && point.z < z + width);
    }

    public void createBranches() {
        final float newWidth = width / 2;
        for (int k = 0; k < 2; k++) {
            final float newZ = z + newWidth * k;
            for (int j = 0; j < 2; j++) {
                final float newY = y + newWidth * j;
                for (int i = 0; i < 2; i++) {
                    final float newX = x + newWidth * i;
                    branches.add(new Octatree(new Vector3D(newX, newY, newZ), newWidth, limit));
                }
            }
        }
    }

    private void addPointToBranch(final Point3D point) {
        final Vector3D pos = point.toVector3D().sub(new Vector3D(x, y, z));

        final int destX = pos.x < width / 2 ? 0 : 1;
        final int destY = pos.y < width / 2 ? 0 : 1;
        final int destZ = pos.z < width / 2 ? 0 : 1;
        branches.get(destX + destY * 2 + destZ * 4).addPoint(point);
    }

    public void addPoint(final Point3D point) {
        if (!inBounds(point)) {
            return;
        }

        points.add(point);

        if (!branched && points.size() == limit) {
            createBranches();
            for (final Point3D branchPoint : points) {
                addPointToBranch(branchPoint);
            }

            branched = true;
            return;
        }

        if (!branched) {
            return;
        }

        addPointToBranch(point);
    }

    public String toString(final int tabs) {
        final String tab = new String(new char[tabs * 2]).replace("\0", " ");
        if (!branched) {
            return tab + "PointAmount: " + points.size() + ",\n";
        }

        final StringBuilder string = new StringBuilder(tab + "Branches : [\n");

        for (final Octatree branch : branches) {
            string.append(branch.toString(tabs + 2));
        }

        return string + tab + "]\n";
    }

    public int getPointAmount() {
        return points.size();
    }

    public boolean isBranched() {
        return branched;
    }

    public List<Octatree> getBranches() {
        return branches;
    }
}














