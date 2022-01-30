package core;

import core.imageBuffers.ImageBuffer2D;
import core.imageBuffers.ImageBuffer3D;
import display.DisplayShader;
import display.RendererShader;
import models.RawModel;
import org.lwjgl.BufferUtils;
import toolbox.Points.Point3D;

import java.nio.ByteBuffer;

import static core.DisplayManager.*;
import static core.GlobalVariables.loader;
import static core.GlobalVariables.world;
import static org.lwjgl.opengl.GL46.*;

public class MasterRenderer {
    private final RawModel quad;

    private final RendererShader renderShader = new RendererShader();
    private final DisplayShader displayShader = new DisplayShader();

    private final int displayBufferID;

    private final ImageBuffer3D worldBuffer;
    private final ImageBuffer3D bitmaskBuffer;

    private final ImageBuffer2D colorAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 0, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
    private final ImageBuffer2D depthAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 1, 2, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
    private final ImageBuffer2D rayDirAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 2, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
    private final ImageBuffer2D frameCountAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 3, 2, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
    private final ImageBuffer2D normalAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 4, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
    private final ImageBuffer2D lightAttachment = new ImageBuffer2D(WIDTH, HEIGHT, 5, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);

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

        displayBufferID = MasterRenderer.createDisplayBuffer();
        MasterRenderer.unbindFrameBuffer();

        screenSizeChange.add(() -> {
            renderShader.start();
            renderShader.loadResolutions();
            renderShader.stop();

            colorAttachment.updateResolution(WIDTH, HEIGHT);
            depthAttachment.updateResolution(WIDTH, HEIGHT);
            rayDirAttachment.updateResolution(WIDTH, HEIGHT);
            frameCountAttachment.updateResolution(WIDTH, HEIGHT);
            normalAttachment.updateResolution(WIDTH, HEIGHT);
            lightAttachment.updateResolution(WIDTH, HEIGHT);
        });

        final Point3D worldScale = world.getWorldScale();
        worldBuffer = new ImageBuffer3D(worldScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);

        final Point3D bitmaskScale = world.getBitmaskScale();
        bitmaskBuffer = new ImageBuffer3D(bitmaskScale, 1, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
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
            final Point3D worldScale = world.getWorldScale();
            final ByteBuffer worldByteBuffer = BufferUtils.createByteBuffer(worldScale.x * worldScale.y * worldScale.z);
            worldByteBuffer.put(world.getWorldBuffer());
            worldByteBuffer.flip();
            worldBuffer.create(worldByteBuffer);

            final Point3D bitmaskScale = world.getBitmaskScale();
            final ByteBuffer bitmaskByteBuffer = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);
            bitmaskByteBuffer.put(world.getBitmaskGrid());
            bitmaskByteBuffer.flip();
            bitmaskBuffer.create(bitmaskByteBuffer);

            worldBuffer.update();
            bitmaskBuffer.update();

            recreateWorldTexture = false;
        }

        renderShader.loadRandomVector();
        renderShader.loadPathTracingSetting();

        // Bind texture buffer
        bindFrameBuffer();

        // Create new texture
        colorAttachment.createAttachment();
        depthAttachment.createAttachment();
        rayDirAttachment.createAttachment();
        frameCountAttachment.createAttachment();
        normalAttachment.createAttachment();
        lightAttachment.createAttachment();

        // Draw Things
        worldBuffer.bind();
        bitmaskBuffer.bind();

        colorAttachment.bind();
        depthAttachment.bind();
        rayDirAttachment.bind();
        frameCountAttachment.bind();
        normalAttachment.bind();
        lightAttachment.bind();

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
        glBindTexture(GL_TEXTURE_2D, colorAttachment.getRecentID());

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        displayShader.stop();

        colorAttachment.update();
        depthAttachment.update();
        rayDirAttachment.update();
        frameCountAttachment.update();
        normalAttachment.update();
        lightAttachment.update();
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

    public void cleanUp() {
        renderShader.cleanUp();

        worldBuffer.delete();
        bitmaskBuffer.delete();

        colorAttachment.delete();
        depthAttachment.delete();
        rayDirAttachment.delete();
        frameCountAttachment.delete();
        normalAttachment.delete();
        lightAttachment.delete();

        glDeleteBuffers(displayBufferID);
    }
}
