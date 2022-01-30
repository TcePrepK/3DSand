package core;

import core.imageBuffers.ImageBuffer3D;
import display.DisplayShader;
import display.RayTracerShader;
import org.lwjgl.BufferUtils;
import toolbox.Points.Point3D;

import java.nio.ByteBuffer;

import static core.GlobalVariables.loader;
import static core.GlobalVariables.world;
import static display.DisplayManager.*;
import static org.lwjgl.opengl.GL46.*;

public class MasterRenderer {
    private final RawModel quad;

    private final RayTracerShader renderShader = new RayTracerShader();
    private final DisplayShader displayShader = new DisplayShader();

    private final int displayBufferID;

    private final ImageBuffer3D worldBuffer;
    private final ImageBuffer3D bitmaskBuffer;

    private final AttachmentManager attachmentManager = new AttachmentManager(WIDTH, HEIGHT);

    private boolean loadCameraVariables = false;
    public boolean recreateWorldTexture = false;
    public boolean reloadResolutions = false;

    public MasterRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadResolutions();
        renderShader.loadBitmaskSize(world.getBitmaskSize());
        ShaderProgram.stop();

        displayBufferID = MasterRenderer.createDisplayBuffer();
        MasterRenderer.unbindFrameBuffer();

        screenSizeChange.add(() -> {
            renderShader.start();
            renderShader.loadResolutions();
            ShaderProgram.stop();

            attachmentManager.updateResolutions(WIDTH, HEIGHT);
        });

        final Point3D worldScale = world.getWorldScale();
        worldBuffer = new ImageBuffer3D(worldScale, 0, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);

        final Point3D bitmaskScale = world.getBitmaskScale();
        bitmaskBuffer = new ImageBuffer3D(bitmaskScale, 1, 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);

        attachmentManager.add("color", 0, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("depth", 1, 2, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
        attachmentManager.add("rayDir", 2, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("frameCount", 3, 2, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
        attachmentManager.add("normal", 4, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("light", 5, 2, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
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
        // Bind texture buffer

        // Create attachments
        attachmentManager.createAttachments();
        // Create attachments

        // Binding attachments
        worldBuffer.bind();
        bitmaskBuffer.bind();

        attachmentManager.bind();
        // Binding attachments

        // Drawing
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_QUAD_STRIP, 0, quad.getVertexCount());
        // Drawing

        // Load old variables
        renderShader.loadOldCameraPos();
        renderShader.loadOldMatrices();
        // Load old variables

        // Unbind texture buffer
        MasterRenderer.unbindFrameBuffer();
        ShaderProgram.stop();
        // Unbind texture buffer

        // Start display
        displayShader.start();
        // Start display

        // Render texture to screen
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, attachmentManager.get("color").getRecentID());
        // Render texture to screen

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

        ShaderProgram.stop();

        attachmentManager.update();
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

        attachmentManager.delete();

        glDeleteBuffers(displayBufferID);
    }
}
