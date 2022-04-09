package game;

import toolbox.Vector3D;

import static core.GlobalVariables.updateSun;

public class Sun {
    private Vector3D position;

    public Sun() {
        position = new Vector3D(1, 0, 0);
    }

    public void update() {
        if (!updateSun) {
            return;
        }

        position = position.rotateZ((float) (Math.PI / 180)).normalize();
    }

    public Vector3D getPosition() {
        return position;
    }
}
