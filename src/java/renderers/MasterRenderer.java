package renderers;

import core.AttachmentManager;
import core.RawModel;
import core.imageBuffers.ImageBuffer3D;
import display.DisplayManager;
import shaders.BaseShader;
import shaders.DisplayShader;
import shaders.RayTracerShader;
import toolbox.Points.Point3D;

import java.nio.ByteBuffer;

import static core.GlobalVariables.loader;
import static core.GlobalVariables.world;
import static display.DisplayManager.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
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
        BaseShader.stop();

        displayShader.start();
        displayShader.loadResolution();
        BaseShader.stop();

        screenSizeChange.add(() -> {
            renderShader.start();
            renderShader.loadResolutions();
            BaseShader.stop();

            displayShader.start();
            displayShader.loadResolution();
            BaseShader.stop();

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

        displayBufferID = createDisplayBuffer();
    }

    public void render() {
//        // Simulation
//        if (currentFrame % 2 == 0 && Keyboard.isKeyDown("Q")) {
//            updateSimulation();
//        }
//        // Simulation

        // Start renderer
        renderShader.start();

        if (reloadResolutions) {
            renderShader.loadResolutions();
            reloadResolutions = false;
        }

        if (loadCameraVariables) {
            renderShader.loadCameraVariables();
            loadCameraVariables = false;
        }

        if (recreateWorldTexture) {
//            final Point3D worldScale = world.getWorldScale();
//            final ByteBuffer worldByteBuffer = BufferUtils.createByteBuffer(world.getWorldBuffer().capacity()).put(world.getWorldBuffer());
//            worldByteBuffer.flip();
            final ByteBuffer worldByteBuffer = world.getWorldBuffer();
            worldByteBuffer.flip();
            worldBuffer.create(worldByteBuffer);
            worldByteBuffer.clear();

//            final Point3D bitmaskScale = world.getBitmaskScale();
//            final ByteBuffer bitmaskByteBuffer = BufferUtils.createByteBuffer(bitmaskScale.x * bitmaskScale.y * bitmaskScale.z);
//            bitmaskByteBuffer.put(world.getBitmaskGrid());
//            bitmaskByteBuffer.flip();
            final ByteBuffer bitmaskByteBuffer = world.getBitmaskGrid();
            bitmaskByteBuffer.flip();
            bitmaskBuffer.create(bitmaskByteBuffer);
            bitmaskByteBuffer.clear();

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
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        // Drawing

        // Unbind texture buffer
        MasterRenderer.unbindFrameBuffer();
        BaseShader.stop();
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
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        BaseShader.stop();

        attachmentManager.update();
    }

    public static void finishRendering() {
        glfwSwapBuffers(DisplayManager.getWindow());
        glfwPollEvents();
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void loadOldCameraVariables() {
        renderShader.start();
        renderShader.loadOldVariables();
        BaseShader.stop();
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, displayBufferID);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

    public static void unbindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

    private int createDisplayBuffer() {
        final int frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        final int size = attachmentManager.size();
        final int[] attachments = new int[size];
        for (int i = 0; i < size; i++) {
            attachments[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        glDrawBuffers(attachments);
        MasterRenderer.unbindFrameBuffer();

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
