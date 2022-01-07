package simulation;

import core.ShaderProgram;
import toolbox.Point3D;

public class SimulationShader extends ShaderProgram {
    private static final String COMPUTE_FILE = "/simulation/simulationComputeShader.glsl";

    private int textureScale;
    private int maxIter;

    public SimulationShader() {
        super(SimulationShader.COMPUTE_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        textureScale = super.getUniformLocation("textureScale");
        maxIter = super.getUniformLocation("maxIter");
    }

    public void loadTextureScale(final Point3D scale) {
        ShaderProgram.load3DVector(textureScale, scale.toVector3D());
    }

    public void loadMaxIter(final float iter) {
        ShaderProgram.loadFloat(maxIter, iter);
    }
}
