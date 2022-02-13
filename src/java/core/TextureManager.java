package core;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class TextureManager {
    // 2D
    public static int create2DTexture(final int width, final int height, final int internalFormat, final int format, final int dataType, final float[] pixels) {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, dataType, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);

        return texture;
    }

    public static int create2DTexture(final int width, final int height, final int internalFormat, final int format, final int dataType, final ByteBuffer pixels) {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, dataType, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);

        return texture;
    }

    public static int create2DTexture(final int width, final int height, final int internalFormat, final int format, final int dataType) {
        return TextureManager.create2DTexture(width, height, internalFormat, format, dataType, (float[]) null);
    }

    public static int create2DTexture(final int width, final int height, final int internalFormat, final int format) {
        return TextureManager.create2DTexture(width, height, internalFormat, format, GL_FLOAT, (float[]) null);
    }
    // 2D

    // 3D
    public static int create3DTexture(final int width, final int height, final int depth, final int internalFormat, final int format, final int dataType, final ByteBuffer pixels) {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texture);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, width, height, depth, 0, format, dataType, pixels);
        glBindTexture(GL_TEXTURE_3D, 0);

        return texture;
    }

    public static int create3DTexture(final int width, final int height, final int depth, final int internalFormat, final int format, final int dataType, final float[] pixels) {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texture);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, width, height, depth, 0, format, dataType, pixels);
        glBindTexture(GL_TEXTURE_3D, 0);

        return texture;
    }

//    public static int create3DTexture(final int width, final int height, final int depth, final int internalFormat, final int format, final int dataType, final ByteBuffer pixels) {
//        final int texture = glGenTextures();
//        glBindTexture(GL_TEXTURE_3D, texture);
//
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
//
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//
//        glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, width, height, depth, 0, format, dataType, pixels);
//        glBindTexture(GL_TEXTURE_3D, 0);
//
//        return texture;
//    }

    public static int create3DTexture(final int width, final int height, final int depth, final int internalFormat, final int format, final int dataType) {
        return TextureManager.create3DTexture(width, height, depth, internalFormat, format, dataType, (float[]) null);
    }

    public static int create3DTexture(final int width, final int height, final int depth, final int internalFormat, final int format) {
        return TextureManager.create3DTexture(width, height, depth, internalFormat, format, GL_FLOAT, (float[]) null);
    }
    // 3D
}
