package display;

import core.TextureManager;
import models.RawModel;
import simulation.SimulationShader;
import toolbox.Keyboard;
import toolbox.Point3D;
import toolbox.Vector3D;

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
    private int oldLightAttachmentId;

    private boolean loadCameraVariables = false;

    public MasterRenderer() {
        final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

        quad = loader.loadToVAO(positions, 2);

        renderShader.start();
        renderShader.loadVariables();
        renderShader.stop();

        simulationShader.start();
        simulationShader.loadTextureScale(world.getWorldScale());
        simulationShader.stop();

        displayBufferId = MasterRenderer.createDisplayBuffer();
        MasterRenderer.unbindFrameBuffer();

        oldColorAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldDepthAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        frameCountAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldRayDirAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldNormalAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);
        oldLightAttachmentId = TextureManager.create2DTexture(WIDTH, HEIGHT, GL_RGB32F, GL_RGB);

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

        final float[] pixels = new float[w * h * d];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    final Vector3D pos = new Vector3D(x, y, z).div(world.getWorldScale().toVector3D());

                    int iter = 0;
                    boolean hit = true;
                    Point3D voxel = pos.mult(3).sub(1).floor().toPoint3D();
                    while (iter <= 4) {
                        final Point3D absVoxel = voxel.abs();
                        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
                            hit = false;
                            break;
                        }

                        iter++;

                        final float power = (float) Math.pow(3, iter);
                        final Vector3D location = pos.mult(power).floor();
                        voxel = location.mod(3).sub(1).toPoint3D();
                    }

                    if (hit) {
                        pixels[x + y * w + z * w * h] = 2;
                    }
                }
            }

//            pixels[x] = 1;
        }

        simulationTextureId = TextureManager.create3DTexture(w, h, d, GL_R8, GL_RED, GL_FLOAT, pixels);

//        updateSimulation();
    }

    public void updateSimulation() {
        simulationShader.start();

        final Point3D scale = world.getWorldScale();
        final int nextSimulationTextureId = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_R8, GL_RED);
        final int lockWorldBuffer = TextureManager.create3DTexture(scale.x, scale.y, scale.z, GL_R32UI, GL_RED, GL_UNSIGNED_BYTE);

        glBindImageTexture(0, simulationTextureId, 0, true, 0, GL_READ_ONLY, GL_R8);
        glBindImageTexture(1, nextSimulationTextureId, 0, true, 0, GL_WRITE_ONLY, GL_R8);
        glBindImageTexture(2, lockWorldBuffer, 0, true, 0, GL_READ_WRITE, GL_RED);
        glDispatchCompute(scale.x, scale.y, scale.z);

        simulationShader.stop();

        glDeleteTextures(simulationTextureId);
        simulationTextureId = nextSimulationTextureId;
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
        final int colorAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 0);
        final int depthAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 1);
        final int rayDirAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 2);
        final int frameCountAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 3);
        final int normalAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 4);
        final int lightAttachment = MasterRenderer.createAttachment(GL_RGBA32F, GL_RGB, 5);

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
        glActiveTexture(GL_TEXTURE6);
        glBindTexture(GL_TEXTURE_2D, oldLightAttachmentId);

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

        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        glDeleteTextures(frameCountAttachmentId);
        glDeleteTextures(oldRayDirAttachmentId);
        glDeleteTextures(oldNormalAttachmentId);
        glDeleteTextures(oldLightAttachmentId);

        oldColorAttachmentId = colorAttachment;
        oldDepthAttachmentId = depthAttachment;
        oldRayDirAttachmentId = rayDirAttachment;
        frameCountAttachmentId = frameCountAttachment;
        oldNormalAttachmentId = normalAttachment;
        oldLightAttachmentId = lightAttachment;
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
        glDeleteTextures(worldTextureId);
        glDeleteTextures(oldColorAttachmentId);
        glDeleteTextures(oldDepthAttachmentId);
        glDeleteTextures(frameCountAttachmentId);
        glDeleteTextures(oldRayDirAttachmentId);
        glDeleteTextures(oldNormalAttachmentId);
        glDeleteTextures(oldLightAttachmentId);
        glDeleteBuffers(displayBufferId);
    }
}
