package models;

public class RawModel {
    private int vaoID;
    private int vertexCount;

    public RawModel(int vaoId, int vertexCount) {
        this.vaoID = vaoId;
        this.vertexCount = vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
