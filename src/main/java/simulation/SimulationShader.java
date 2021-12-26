package simulation;

import core.ShaderProgram;

public class SimulationShader extends ShaderProgram {
    private static final String COMPUTE_FILE = "/simulation/simulationComputeShader.glsl";

    public SimulationShader() {
        super(SimulationShader.COMPUTE_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {

    }
}
