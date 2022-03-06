package toolbox;

import java.nio.ByteBuffer;

public class BitManager {
    private final int width, height, depth;
    private final int bitWidth, bitHeight, bitDepth;
    private final ByteBuffer byteBuffer;

    private final int inputSize;
    private final int inputPerByte;

    public BitManager(final int width, final int height, final int depth, final int inputSize) {
        if (inputSize > 8) {
            Logger.error("Tried to use more data than 8 as a size for BitManager!");
        }

        inputPerByte = 8 / inputSize;
        this.width = width / inputPerByte;
        this.height = height / inputPerByte;
        this.depth = depth / inputPerByte;

        bitWidth = (int) Math.floor(Math.pow(inputPerByte, 1 / 3f));
        bitHeight = (int) Math.floor(Math.sqrt(inputPerByte / (float) bitWidth));
        bitDepth = inputPerByte / (bitWidth * bitHeight);

        this.inputSize = inputSize;
        byteBuffer = ByteBuffer.allocate(this.width * this.height * this.depth);

        System.out.println(bitWidth);
        System.out.println(bitHeight);
        System.out.println(bitDepth);
    }

//    public int readValue(final int index) {
//        return readByte(getByteIndex(index));
//    }

    public void writeValue(final Vector3D pos, final int num) {
        final int byteIndex = getByteIndex(getBytePos(pos));
//        System.out.println(byteIndex);
//        int targetedByte = readByte(byteIndex);
//        for (int i = 0; i < inputSize; i++) {
//            final int pos = (index % inputPerByte) * inputSize + i;
//            final int bit = (num & (1 << i)) >> i;
//            targetedByte = targetedByte & ~(1 << pos) | (bit << pos);
//        }

//        writeByte(byteIndex, (byte) targetedByte);
    }

    public byte readByte(final int byteIndex) {
        return byteBuffer.get(byteIndex);
    }

    public void writeByte(final int byteIndex, final byte val) {
        byteBuffer.put(byteIndex, val);
    }

    private Vector3D getBytePos(final Vector3D indexPos) {
        return indexPos.div(new Vector3D(bitWidth, bitHeight, bitDepth)).floor();
    }

    private int getByteIndex(final Vector3D bytePos) {
        return (int) (bytePos.x + bytePos.y * width + bytePos.z * width * height);
    }
}
