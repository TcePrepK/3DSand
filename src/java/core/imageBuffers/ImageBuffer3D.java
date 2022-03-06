package core.imageBuffers;

import core.TextureManager;
import toolbox.Points.Point3D;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

public class ImageBuffer3D extends ImageBuffer {
    private final int width, height, depth;

    public ImageBuffer3D(final int width, final int height, final int depth, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        super(location, offset, internalFormat, format, dataType);

        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public ImageBuffer3D(final Point3D scale, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        super(location, offset, internalFormat, format, dataType);

        width = scale.x;
        height = scale.y;
        depth = scale.z;
    }

    public void updatePixels(final ByteBuffer pixels) {
        glBindTexture(GL_TEXTURE_3D, id);
        glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, width, height, depth, 0, format, dataType, pixels);
        glBindTexture(GL_TEXTURE_3D, 0);
    }

    @Override
    public void create(final ByteBuffer pixels) {
        id = TextureManager.create3DTexture(width, height, depth, internalFormat, format, dataType, pixels);
    }

    @Override
    public void bind() {
        glActiveTexture(GL_TEXTURE0 + location + offset);
        glBindTexture(GL_TEXTURE_3D, id);
    }
}
