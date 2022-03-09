package core;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class SSBO {
    private int id;

    private final int index, usage;

    public SSBO(final int index, final int usage) {
        this.index = index;
        this.usage = usage;
    }

    public void create(final long[] data) {
        id = glGenBuffers();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBufferData(GL_SHADER_STORAGE_BUFFER, data, usage);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, index, id);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void bind() {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, index, id);
    }
}
