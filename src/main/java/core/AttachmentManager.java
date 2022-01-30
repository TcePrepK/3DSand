package core;

import core.imageBuffers.ImageBuffer2D;

import java.util.HashMap;
import java.util.Map;

public class AttachmentManager {
    private int width, height;
    private final Map<String, ImageBuffer2D> imageBufferList = new HashMap<>();

    public AttachmentManager(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void update() {
        for (final ImageBuffer2D imageBuffer : imageBufferList.values()) {
            imageBuffer.update();
        }
    }

    public void bind() {
        for (final ImageBuffer2D imageBuffer : imageBufferList.values()) {
            imageBuffer.bind();
        }
    }

    public void createAttachments() {
        for (final ImageBuffer2D imageBuffer : imageBufferList.values()) {
            imageBuffer.createAttachment();
        }
    }

    public void delete() {
        for (final ImageBuffer2D imageBuffer : imageBufferList.values()) {
            imageBuffer.delete();
        }
    }

    public void add(final String id, final int location, final int offset, final int internalFormat, final int format, final int dataType) {
        final ImageBuffer2D image = new ImageBuffer2D(width, height, location, offset, internalFormat, format, dataType);
        imageBufferList.put(id, image);
    }

    public ImageBuffer2D get(final String id) {
        return imageBufferList.get(id);
    }

    public void remove(final String id) {
        imageBufferList.remove(id);
    }

    public void updateResolutions(final int width, final int height) {
        this.width = width;
        this.height = height;

        for (final ImageBuffer2D imageBuffer : imageBufferList.values()) {
            imageBuffer.updateResolution(width, height);
        }
    }
}
