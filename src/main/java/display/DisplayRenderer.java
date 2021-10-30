package display;

import models.RawModel;
import toolbox.Point3D;

import java.nio.ByteBuffer;

import static core.GlobalVariables.loader;
import static core.GlobalVariables.world;
import static org.lwjgl.opengl.GL46.*;

public class DisplayRenderer {
    private final RawModel quad;
    private final DisplayShader shader = new DisplayShader();
    private int textureId;

    private boolean loadCameraVariables = false;

    public DisplayRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        shader.start();
        shader.loadScreenVariables();
    }

    public void render() {
        if (loadCameraVariables) {
            shader.loadCameraVariables();
            loadCameraVariables = false;
        }

        create3DTexture();

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void create3DTexture() {
        glDeleteTextures(textureId);

        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texture);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        final Point3D scale = world.getBufferScale();
        glTexImage3D(GL_TEXTURE_3D, 0, GL_RED, scale.x, scale.y, scale.z, 0, GL_RED, GL_UNSIGNED_BYTE, world.getWorldBuffer());

        textureId = texture;
    }

    public static int create2DTexture(final ByteBuffer byteBuffer) {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, byteBuffer);
        return texture;
    }

    public void cleanUp() {
        shader.cleanUp();
        glDeleteTextures(textureId);
    }
}
