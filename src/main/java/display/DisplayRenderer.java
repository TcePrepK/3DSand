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
    private int worldTextureId;
    private final int displayBufferId;
    private int oldColorAttachmentId;
    private int oldDepthAttachmentId;

    private boolean loadCameraVariables = false;
    private int framesWithoutUpdate = 0;

    public DisplayRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadVariables();
        renderShader.stop();

        displayBufferId = DisplayRenderer.createDisplayBuffer();
        DisplayRenderer.unbindFrameBuffer();

        oldColorAttachmentId = DisplayRenderer.create2DTexture();
        oldDepthAttachmentId = DisplayRenderer.create2DTexture();
    }

    public void render() {
        // Start renderer
        renderShader.start();

        // Load variables
        if (loadCameraVariables) {
            renderShader.loadCameraVariables();
            loadCameraVariables = false;

            oldColorAttachmentId = DisplayRenderer.create2DTexture();
            oldDepthAttachmentId = DisplayRenderer.create2DTexture();
            framesWithoutUpdate = 0;
        }

        renderShader.loadRandomVector();
        renderShader.loadColorWeights(framesWithoutUpdate++);

        // Bind texture buffer
        bindFrameBuffer();

        // Create new texture
        final int colorAttachment = DisplayRenderer.createColorAttachment();
        final int depthAttachment = DisplayRenderer.createDepthAttachment();

        // Draw Things
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, create3DTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, oldColorAttachmentId);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, oldDepthAttachmentId);

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
        glBindTexture(GL_TEXTURE_2D, colorAttachment);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        displayShader.stop();

        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        oldColorAttachmentId = colorAttachment;
        oldDepthAttachmentId = depthAttachment;
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, displayBufferId);
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }

    public static void unbindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }

    public int create3DTexture() {
        glDeleteTextures(worldTextureId);

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

        worldTextureId = texture;
        return texture;
    }

    private static int createDisplayBuffer() {
        final int frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        final int[] attachments = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
        glDrawBuffers(attachments);
        return frameBuffer;
    }

    public static int create2DTexture() {
        final int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);

        return texture;
    }

    public static int createColorAttachment() {
        final int texture = DisplayRenderer.create2DTexture();
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture, 0);

        return texture;
    }

    public static int createDepthAttachment() {
        final int texture = DisplayRenderer.create2DTexture();
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, texture, 0);

        return texture;
    }

    public void cleanUp() {
        renderShader.cleanUp();
        glDeleteTextures(worldTextureId);
        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        glDeleteBuffers(displayBufferId);
    }
}
