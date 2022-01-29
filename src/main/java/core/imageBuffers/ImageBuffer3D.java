package core.imageBuffers;

import core.TextureManager;
import toolbox.Points.Point3D;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

public class ImageBuffer3D {
    private final int width, height, depth;
    private final int location;
    private final int offset;
    private final int internalFormat, format, dataType;
    private int id, oldID;

    public ImageBuffer3D(final int width, final int height, final int depth, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.location = location;
        this.offset = offset;

        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
    }

    public ImageBuffer3D(final Point3D scale, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        width = scale.x;
        height = scale.y;
        depth = scale.z;

        this.location = location;
        this.offset = offset;

        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
    }

    public void create(final ByteBuffer pixels) {
        id = TextureManager.create3DTexture(width, height, depth, internalFormat, format, dataType, pixels);
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

    public void bind() {
        glActiveTexture(GL_TEXTURE0 + location + offset);
        glBindTexture(GL_TEXTURE_3D, oldID);
    }

    public void delete() {
        glDeleteTextures(id);
        glDeleteTextures(oldID);
    }

    public int getRecentID() {
        return id;
    }
}
