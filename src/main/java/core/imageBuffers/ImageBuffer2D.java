package core.imageBuffers;

import core.TextureManager;
import toolbox.Points.Point2D;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

public class ImageBuffer2D {
    private int width, height;
    private final int location;
    private final int offset;
    private final int internalFormat, format, dataType;
    private int id, oldID;

    public ImageBuffer2D(final int width, final int height, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        this.width = width;
        this.height = height;

        this.location = location;
        this.offset = offset;

        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
    }

    public ImageBuffer2D(final Point2D scale, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        width = scale.x;
        height = scale.y;

        this.location = location;
        this.offset = offset;

        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
    }

    public void create(final ByteBuffer pixels) {
        id = TextureManager.create2DTexture(width, height, internalFormat, format, dataType, pixels);
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

    public void updateResolution(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0 + location + offset);
        glBindTexture(GL_TEXTURE_2D, oldID);
    }

    public void delete() {
        glDeleteTextures(id);
        glDeleteTextures(oldID);
    }

    public int getRecentID() {
        return id;
    }
}
