package display;

import models.RawModel;
import toolbox.Point3D;

import java.nio.ByteBuffer;

import static core.GlobalVariables.loader;
import static core.GlobalVariables.world;
import static org.lwjgl.opengl.GL46.*;

public class DisplayRenderer {
    private final RawModel quad;
    private final RendererShader renderShader = new RendererShader();
    private final DisplayShader displayShader = new DisplayShader();
    private int textureId;
    private int oldTextureId;
    private final int displayBufferId;

    private boolean loadCameraVariables = false;

    public DisplayRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadVariables();
        renderShader.stop();

        displayBufferId = DisplayRenderer.createDisplayBuffer();
        oldTextureId = DisplayRenderer.createDisplayTexture();
        DisplayRenderer.unbindFrameBuffer();
    }

    public void render() {
        // Start renderer
        renderShader.start();

        // Load variables
        if (loadCameraVariables) {
            renderShader.loadCameraVariables();
            loadCameraVariables = false;
        }

        // Bind texture buffer
        bindFrameBuffer();

        // Create new texture
        final int textureId = DisplayRenderer.createDisplayTexture();

        // Draw Things
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, create3DTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, oldTextureId);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        // Unbind texture buffer
        DisplayRenderer.unbindFrameBuffer();

        // Stop renderer and start display
        renderShader.stop();
        displayShader.start();

        // Render texture to screen
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        displayShader.stop();

        glDeleteTextures(oldTextureId);
        oldTextureId = textureId;
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, displayBufferId);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }

    public static void unbindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }

    public int create3DTexture() {
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
        glBindTexture(GL_TEXTURE_3D, 0);

        textureId = texture;
        return texture;
    }

    private static int createDisplayBuffer() {
        final int frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        return frameBuffer;
    }

    public static int createDisplayTexture() {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture, 0);

        return texture;
    }

    public void cleanUp() {
        renderShader.cleanUp();
        glDeleteTextures(textureId);
        glDeleteTextures(oldTextureId);
        glDeleteBuffers(displayBufferId);
    }
}
