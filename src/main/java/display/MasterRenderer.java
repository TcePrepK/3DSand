package display;

import core.TextureManager;
import models.RawModel;
import org.lwjgl.BufferUtils;
import simulation.SimulationShader;
import toolbox.Keyboard;
import toolbox.Point3D;

import java.nio.ByteBuffer;

import static core.DisplayManager.*;
import static core.GlobalVariables.*;
import static org.lwjgl.opengl.GL46.*;

public class MasterRenderer {
    private final RawModel quad;

    private final RendererShader renderShader = new RendererShader();
    private final DisplayShader displayShader = new DisplayShader();
    private final SimulationShader simulationShader = new SimulationShader();

    private int worldTextureID;
    private int bitmaskTextureID;
    private int simulationTextureID;
    private final int displayBufferID;

    private int oldColorAttachmentID;
    private int oldDepthAttachmentID;
    private int frameCountAttachmentID;
    private int oldRayDirAttachmentID;
    private int oldNormalAttachmentID;
    private int oldLightAttachmentID;

    private boolean loadCameraVariables = false;
    public boolean recreateWorldTexture = false;
    public boolean reloadResolutions = false;

    public MasterRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadResolutions();
        renderShader.loadBitmaskSize(world.getBitmaskSize());
        renderShader.stop();

        simulationShader.start();
        simulationShader.loadTextureScale(world.getWorldScale());
        simulationShader.stop();

        displayBufferID = MasterRenderer.createDisplayBuffer();
        MasterRenderer.unbindFrameBuffer();

        oldColorAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldDepthAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldRayDirAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        frameCountAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_R8, GL_RGB);
        oldNormalAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldLightAttachmentID = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);

        world.setBufferSize();

        Keyboard.keyPressed.add(() -> {
            renderingFractal = !renderingFractal;
            loadCameraVariables = true;

            world.setBufferSize();
        }, "m");

        screenSizeChange.add(() -> {
            renderShader.start();
            renderShader.loadResolutions();
            renderShader.stop();
        });
    }

    public void updateSimulation() {
        simulationShader.start();

        final Point3D scale = world.getWorldScale();
        final int nextSimulationTextureId = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_R8, GL_RED);
        final int lockWorldBuffer = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_R32UI, GL_RED, GL_UNSIGNED_BYTE);

        glBindImageTexture(0, simulationTextureID, 0, true, 0, GL_READ_ONLY, GL_R8);
        glBindImageTexture(1, nextSimulationTextureId, 0, true, 0, GL_WRITE_ONLY, GL_R8);
        glBindImageTexture(2, lockWorldBuffer, 0, true, 0, GL_READ_WRITE, GL_RED);
        glDispatchCompute(scale.x, scale.y, scale.z);

        simulationShader.stop();

        glDeleteTextures(simulationTextureID);
        simulationTextureID = nextSimulationTextureId;
    }

    public void render() {
//        // Simulation
//        if (currentFrame % 2 == 0 && Keyboard.isKeyDown("Q")) {
//            updateSimulation();
//        }
//        // Simulation

        // Start renderer
        renderShader.start();

        // Load variables
        renderShader.setResetEverything(loadCameraVariables);

        if (reloadResolutions) {
            renderShader.loadResolutions();
            reloadResolutions = false;
        }

        if (loadCameraVariables) {
            renderShader.loadCameraVariables();
            loadCameraVariables = false;
        }

        if (recreateWorldTexture) {
            glDeleteTextures(worldTextureID);
            glDeleteTextures(bitmaskTextureID);

            final Point3D worldScale = world.getWorldScale();
            final ByteBuffer worldBuffer = BufferUtils.createByteBuffer(worldScale.x * worldScale.y * worldScale.z);
            worldBuffer.put(world.getWorldBuffer());
            worldBuffer.flip();
            worldTextureID = TextureManager.create3DTexture(worldScale.x, worldScale.y, worldScale.z, GL_R8, GL_RED, GL_UNSIGNED_BYTE, worldBuffer);

            final Point3D bitmaskScale = world.getBitmaskScale();
            final ByteBuffer bitmaskBuffer = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);
            bitmaskBuffer.put(world.getBitmaskGrid());
            bitmaskBuffer.flip();
            bitmaskTextureID = TextureManager.create3DTexture(bitmaskScale.x, bitmaskScale.y, bitmaskScale.z, GL_R8, GL_RED, GL_UNSIGNED_BYTE, bitmaskBuffer);

            recreateWorldTexture = false;
        }

        renderShader.loadRandomVector();

        // Bind texture buffer
        bindFrameBuffer();

        // Create new texture
        final int colorAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 0);
        final int depthAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 1);
        final int rayDirAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 2);
        final int frameCountAttachment = MasterRenderer.createAttachment(GL_R8, GL_RGB, 3);
        final int normalAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 4);
        final int lightAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 5);

        // Draw Things
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, worldTextureID);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_3D, bitmaskTextureID);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, oldColorAttachmentID);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, oldDepthAttachmentID);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, oldRayDirAttachmentID);
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, frameCountAttachmentID);
        glActiveTexture(GL_TEXTURE6);
        glBindTexture(GL_TEXTURE_2D, oldNormalAttachmentID);
        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_2D, oldLightAttachmentID);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_QUAD_STRIP, 0, quad.getVertexCount());

        renderShader.loadOldCameraPos();
        renderShader.loadOldMatrices();

        // Unbind texture buffer
        MasterRenderer.unbindFrameBuffer();
        renderShader.stop();

        // Start display
        displayShader.start();

        // Render texture to screen
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorAttachment);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        displayShader.stop();

        glDeleteTextures(oldColorAttachmentID);
        glDeleteTextures(oldDepthAttachmentID);
        glDeleteTextures(frameCountAttachmentID);
        glDeleteTextures(oldRayDirAttachmentID);
        glDeleteTextures(oldNormalAttachmentID);
        glDeleteTextures(oldLightAttachmentID);

        oldColorAttachmentID = colorAttachment;
        oldDepthAttachmentID = depthAttachment;
        oldRayDirAttachmentID = rayDirAttachment;
        frameCountAttachmentID = frameCountAttachment;
        oldNormalAttachmentID = normalAttachment;
        oldLightAttachmentID = lightAttachment;
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, displayBufferID);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

    public static void unbindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

    private static int createDisplayBuffer() {
        final int frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        final int[] attachments = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5};
        glDrawBuffers(attachments);
        return frameBuffer;
    }

    public static int createAttachment(final int internalFormat, final int format, final int id) {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, internalFormat, format, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + id, texture, 0);

        return texture;
    }

    public void cleanUp() {
        renderShader.cleanUp();
        glDeleteTextures(worldTextureID);
        glDeleteTextures(oldColorAttachmentID);
        glDeleteTextures(oldDepthAttachmentID);
        glDeleteTextures(frameCountAttachmentID);
        glDeleteTextures(oldRayDirAttachmentID);
        glDeleteTextures(oldNormalAttachmentID);
        glDeleteTextures(oldLightAttachmentID);
        glDeleteBuffers(displayBufferID);
    }
}
