package core.imageBuffers;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

public abstract class ImageBuffer {
    protected final int location;
    protected final int offset;
    protected final int internalFormat, format, dataType;
    protected int id, oldID;

    public ImageBuffer(final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        this.location = location;
        this.offset = offset;

        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
    }

    public void createAttachment(final ByteBuffer pixels) {
        create(pixels);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + location, id, 0);
    }

    public void createAttachment() {
        createAttachment(null);
    }

    public void update() {
        glDeleteTextures(oldID);
        oldID = id;
    }

    public void delete() {
        glDeleteTextures(id);
        glDeleteTextures(oldID);
    }

    abstract public void create(final ByteBuffer pixels);

    abstract public void bind();

    public boolean isEmpty() {
        return id == 0 && oldID == 0;
    }

    public int getID() {
        return id;
    }

    public int getOldID() {
        return oldID;
    }

    public int getPosition() {
        return location + offset;
    }
}
