package core;

import core.imageBuffers.ImageBuffer;
import core.imageBuffers.ImageBuffer2D;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AttachmentManager {
    private int width, height;
    private final Map<String, ImageBuffer2D> imageBufferList = new HashMap<>();

    public AttachmentManager(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void update() {
        forEach(ImageBuffer2D::update);
    }

    public void createAttachments() {
        forEach(ImageBuffer2D::createAttachment);
    }

    public void bind() {
        forEach(ImageBuffer2D::bind);
    }

    public void updateResolutions(final int width, final int height) {
        this.width = width;
        this.height = height;

        forEach(imageBuffer -> imageBuffer.updateResolution(width, height));
    }

    public void add(final String id, final int offset, final int internalFormat, final int format, final int dataType) {
        final ImageBuffer2D image = new ImageBuffer2D(width, height, imageBufferList.size(), offset, internalFormat, format, dataType);
        imageBufferList.put(id, image);
    }

    public ImageBuffer2D get(final String id) {
        return imageBufferList.get(id);
    }

    public void remove(final String id) {
        imageBufferList.remove(id);
    }

    public void delete() {
        forEach(ImageBuffer::delete);
    }

    public void forEach(final Consumer<ImageBuffer2D> func) {
        imageBufferList.values().forEach(func);
    }

    public int size() {
        return imageBufferList.size();
    }

    public String[] keys() {
        return imageBufferList.keySet().toArray(new String[0]);
    }
}
