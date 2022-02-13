package core.imageBuffers;

import core.TextureManager;
import toolbox.Points.Point2D;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

public class ImageBuffer2D extends ImageBuffer {
    private int width, height;

    public ImageBuffer2D(final int width, final int height, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        super(location, offset, internalFormat, format, dataType);

        this.width = width;
        this.height = height;
    }

    public ImageBuffer2D(final Point2D scale, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        super(location, offset, internalFormat, format, dataType);

        width = scale.x;
        height = scale.y;
    }

    @Override
    public void create(final ByteBuffer pixels) {
        id = TextureManager.create2DTexture(width, height, internalFormat, format, dataType, pixels);
    }

    @Override
    public void bind() {
        glActiveTexture(GL_TEXTURE0 + location + offset);
        glBindTexture(GL_TEXTURE_2D, oldID);
    }

    public void updateResolution(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
}
