package core;

public class RawModel {
    private final int vaoID;
    private final int vertexCount;

    public RawModel(final int vaoId, final int vertexCount) {
        vaoID = vaoId;
        this.vertexCount = vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
