package core;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    private final List<Integer> vaos = new ArrayList<>();
    private final List<Integer> vbos = new ArrayList<>();
    private final List<Integer> textures = new ArrayList<>();

    public Loader() {
        GlobalVariables.loader = this;
    }

    public RawModel loadToVAO(final float[] positions, final int dimensions) {
        final int vaoID = createVAO();

        storeDataInAttributeList(0, dimensions, positions);

        Loader.unbindVAO();
        return new RawModel(vaoID, positions.length / dimensions);
    }

    public RawModel loadToVAO(final float[] positions, final int[] indices) {
        final int vaoID = createVAO();

        bindIndicesBuffer(indices);

        storeDataInAttributeList(0, 3, positions);

        Loader.unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    public RawModel loadToVAO(final float[] positions, final byte[] colors, final int[] indices) {
        final int vaoID = createVAO();

        bindIndicesBuffer(indices);

        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 4, colors);

        Loader.unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    public void cleanUp() {
        for (final int vao : vaos) {
            GL30.glDeleteVertexArrays(vao);
        }

        for (final int vbo : vbos) {
            GL15.glDeleteBuffers(vbo);
        }

        for (final int texture : textures) {
            GL11.glDeleteTextures(texture);
        }
    }

    private int createVAO() {
        final int vaoID = GL30.glGenVertexArrays();

        vaos.add(vaoID);

        GL30.glBindVertexArray(vaoID);

        return vaoID;
    }

    private void storeDataInAttributeList(final int attributeNumber, final int coordinateSize, final float[] data) {
        final int vboID = GL15.glGenBuffers();
        final FloatBuffer buffer = Loader.storeDataInFloatBuffer(data);

        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void storeDataInAttributeList(final int attributeNumber, final int coordinateSize, final byte[] data) {
        final int vboID = GL15.glGenBuffers();
        final ByteBuffer buffer = Loader.storeDataInByteBuffer(data);

        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_BYTE, true, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    private void bindIndicesBuffer(final int[] indices) {
        final int vboID = GL15.glGenBuffers();
        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        final IntBuffer buffer = Loader.storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private static ByteBuffer storeDataInByteBuffer(final byte[] data) {
        final ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
        buffer.put(data);
        buffer.flip();

        return buffer;
    }

    private static IntBuffer storeDataInIntBuffer(final int[] data) {
        final IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();

        return buffer;
    }

    private static FloatBuffer storeDataInFloatBuffer(final float[] data) {
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);

        buffer.put(data);
        buffer.flip();

        return buffer;
    }
}
