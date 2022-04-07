package renderers;

import core.AttachmentManager;
import core.RawModel;
import core.SSBO;
import core.imageBuffers.ImageBuffer2D;
import display.DisplayManager;
import shaders.BaseShader;
import shaders.DisplayShader;
import shaders.RayTracerShader;

import static core.GlobalVariables.*;
import static display.DisplayManager.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL46.*;

public class MasterRenderer {
    private final RawModel quad;

    private final RayTracerShader renderShader = new RayTracerShader();
    private final DisplayShader displayShader = new DisplayShader();

    private final int displayBufferID;

    private final AttachmentManager attachmentManager = new AttachmentManager(WIDTH, HEIGHT);
    private final SSBO chunkBuffer = new SSBO(0, GL_DYNAMIC_READ);
    private final SSBO bitmaskBuffer = new SSBO(1, GL_DYNAMIC_READ);

    private boolean loadCameraVariables = false;
    public boolean reloadResolutions = false;

    public MasterRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        displayShader.start();
        displayShader.loadResolution();
        BaseShader.stop();

        renderShader.start();
        renderShader.loadResolutions();
        renderShader.loadChunkScale();

        attachmentManager.add("color", 0, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("depth", 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
        attachmentManager.add("rayDir", 0, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("frameCount", 0, GL_R8, GL_RED, GL_UNSIGNED_BYTE);
        attachmentManager.add("normal", 0, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);
        attachmentManager.add("light", 0, GL_RGB32F, GL_RGB, GL_UNSIGNED_BYTE);

        for (final String key : attachmentManager.keys()) {
            final ImageBuffer2D imageBuffer = attachmentManager.get(key);
            renderShader.bindTexture(key + "Attachment", imageBuffer.getPosition());
        }

        BaseShader.stop();

        displayBufferID = createDisplayBuffer();

        screenSizeChange.add(() -> {
            renderShader.start();
            renderShader.loadResolutions();
            BaseShader.stop();

            displayShader.start();
            displayShader.loadResolution();
            BaseShader.stop();

            attachmentManager.updateResolutions(WIDTH, HEIGHT);
        });
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

//        if (recreateWorldTexture) {
//            final ByteBuffer worldByteBuffer = world.getWorldBuffer();
//            worldByteBuffer.flip();
//            worldBuffer.updatePixels(worldByteBuffer);
//            worldByteBuffer.clear();
//
//            final ByteBuffer bitmaskByteBuffer = world.getBitmaskGrid();
//            bitmaskByteBuffer.flip();
//            bitmaskBuffer.updatePixels(bitmaskByteBuffer);
//            bitmaskByteBuffer.clear();
//
//            recreateWorldTexture = false;
//        }

        renderShader.loadRandomVector();
        renderShader.loadPathTracingSetting();
        renderShader.loadBitmaskBorderSetting();
        renderShader.loadLightBounceAmount();

        // Bind texture buffer
        bindFrameBuffer();
        // Bind texture buffer

        // Create attachments
        attachmentManager.createAttachments();
        // Create attachments

        // Binding attachments
        chunkBuffer.create(chunkManager.getVoxelBufferIDArray());
        bitmaskBuffer.create(chunkManager.getBitmaskBufferIDArray());

        attachmentManager.bind();

        chunkBuffer.bind();
        bitmaskBuffer.bind();
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
        glBindTexture(GL_TEXTURE_2D, attachmentManager.get(outputOption).getID());
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
        attachmentManager.delete();
        chunkManager.cleanUp();

        glDeleteBuffers(displayBufferID);
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }
}
