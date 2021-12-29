package display;

import core.TextureManager;
import models.RawModel;
import simulation.SimulationShader;
import toolbox.Keyboard;
import toolbox.Point3D;

import static core.DisplayManager.HEIGHT;
import static core.DisplayManager.WIDTH;
import static core.GlobalVariables.*;
import static org.lwjgl.opengl.GL46.*;

public class MasterRenderer {
    private final RawModel quad;

    private final RendererShader renderShader = new RendererShader();
    private final DisplayShader displayShader = new DisplayShader();
    private final SimulationShader simulationShader = new SimulationShader();

    private int worldTextureId;
    private int simulationTextureId;
    private final int displayBufferId;

    private int oldColorAttachmentId;
    private int oldDepthAttachmentId;
    private int frameCountAttachmentId;
    private int oldRayDirAttachmentId;
    private int oldNormalAttachmentId;

    private boolean loadCameraVariables = false;

    public MasterRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadVariables();
        renderShader.stop();

        displayBufferId = MasterRenderer.createDisplayBuffer();
        MasterRenderer.unbindFrameBuffer();

        oldColorAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        oldDepthAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        frameCountAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        oldRayDirAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        oldNormalAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);

        world.setBufferSize();

        Keyboard.keyPressed.add(() -> {
            renderingFractal = !renderingFractal;
            loadCameraVariables = true;

            world.setBufferSize();
        }, "m");

        // Temporary
        final int w = world.getWorldScale().x;
        final int h = world.getWorldScale().y;
        final int d = world.getWorldScale().z;

        final float[] pixels = new float[w * h * d * 4];
        for (int i = 0; i < w * h * d; i++) {
            if (rand.nextFloat() < 0.01) {
                pixels[i * 4] = 1;
                pixels[i * 4 + 1] = 1;
                pixels[i * 4 + 2] = 1;
                pixels[i * 4 + 3] = 1;
            }
        }

//        for (int x = w / 2 - 1; x < w / 2 + 1; x++) {
//            for (int y = 0; y < h / 2; y++) {
//                for (int z = d / 2 - 1; z < d / 2 + 1; z++) {
//                    if (rand.nextFloat() < 0.1) {
//                        final int idx = 4 * (x + (y * w) + (z * w * h));
//                        pixels[idx] = 1;
//                    }
//                }
//            }
//        }

        final Point3D scale = world.getWorldScale();
        simulationTextureId = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_RGBA32F, GL_RGBA, GL_FLOAT, pixels);
    }

    public void render() {
        // Simulation
        if (currentFrame % 2 == 0 && Keyboard.isKeyDown("Q")) {
            simulationShader.start();

            final Point3D scale = world.getWorldScale();
            final int nextSimulationTextureId = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_RGBA32F, GL_RGBA);
            final int lockWorldBuffer = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_R32UI, GL_RED, GL_UNSIGNED_BYTE);

            glBindImageTexture(0, simulationTextureId, 0, true, 0, GL_READ_ONLY, GL_RGBA32F);
            glBindImageTexture(1, nextSimulationTextureId, 0, true, 0, GL_WRITE_ONLY, GL_RGBA32F);
            glBindImageTexture(2, lockWorldBuffer, 0, true, 0, GL_READ_WRITE, GL_RED);
            glDispatchCompute(scale.x, scale.y, scale.z);

            simulationShader.stop();

            glDeleteTextures(simulationTextureId);
            simulationTextureId = nextSimulationTextureId;
        }
        // Simulation

        // Start renderer
        renderShader.start();

        // Load variables
        renderShader.setResetEverything(loadCameraVariables);

        if (loadCameraVariables) {
            renderShader.loadCameraVariables();
            loadCameraVariables = false;

//            oldColorAttachmentId = DisplayRenderer.create2DTexture();
//            oldDepthAttachmentId = DisplayRenderer.create2DTexture();
//            frameCounter = 0;
        }

        renderShader.loadRandomVector();

        // Bind texture buffer
        bindFrameBuffer();

        // Create new texture
        final int colorAttachment = MasterRenderer.createColorAttachment();
        final int depthAttachment = MasterRenderer.createDepthAttachment();
        final int frameCountAttachment = MasterRenderer.createFrameCountAttachment();
        final int rayDirAttachment = MasterRenderer.createRayDirAttachment();
        final int normalAttachment = MasterRenderer.createNormalAttachment();

        // Draw Things
        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_3D, create3DTexture(world.getWorldScale(), world.getWorldBuffer()));
        glBindTexture(GL_TEXTURE_3D, simulationTextureId);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, oldColorAttachmentId);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, oldDepthAttachmentId);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, oldRayDirAttachmentId);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, frameCountAttachmentId);
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, oldNormalAttachmentId);

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

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

        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        glDeleteTextures(frameCountAttachmentId);
        glDeleteTextures(oldRayDirAttachmentId);
        glDeleteTextures(oldNormalAttachmentId);

        oldColorAttachmentId = colorAttachment;
        oldDepthAttachmentId = depthAttachment;
        oldRayDirAttachmentId = rayDirAttachment;
        frameCountAttachmentId = frameCountAttachment;
        oldNormalAttachmentId = normalAttachment;
    }

    public void loadCameraVariablesNextFrame() {
        loadCameraVariables = true;
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, displayBufferId);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

    public static void unbindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, WIDTH, HEIGHT);
    }

//    private int create3DTexture(final Point3D scale, final float[] buffer) {
////        glDeleteTextures(worldTextureId);
//
//        final int texture = glGenTextures();
//        glBindTexture(GL_TEXTURE_3D, texture);
//
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_LINEAR);
//
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//
//        glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA32F, scale.x, scale.y, scale.z, 0, GL_RGBA, GL_FLOAT, buffer);
//        glBindTexture(GL_TEXTURE_3D, 0);
//
//        worldTextureId = texture;
//        return texture;
//    }

    private static int createDisplayBuffer() {
        final int frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        final int[] attachments = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5};
        glDrawBuffers(attachments);
        return frameBuffer;
    }

    public static int createColorAttachment() {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture, 0);

        return texture;
    }

    public static int createDepthAttachment() {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, texture, 0);

        return texture;
    }

    public static int createRayDirAttachment() {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, texture, 0);

        return texture;
    }

    public static int createFrameCountAttachment() {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT3, texture, 0);

        return texture;
    }

    public static int createNormalAttachment() {
        final int texture = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB, GL_FLOAT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT4, texture, 0);

        return texture;
    }

    public void cleanUp() {
        renderShader.cleanUp();
        glDeleteTextures(worldTextureId);
        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        glDeleteTextures(frameCountAttachmentId);
        glDeleteTextures(oldRayDirAttachmentId);
        glDeleteTextures(oldNormalAttachmentId);
        glDeleteBuffers(displayBufferId);
    }
}
