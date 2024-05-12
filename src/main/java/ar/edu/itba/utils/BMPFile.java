package ar.edu.itba.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BMPFile {
    private static final Integer BMP_HEADER_SIZE = 54;
    private static final Integer BMP_SIZE_INDEX_START = 2;
    private static final Integer BMP_SIZE_BYTES = 4;
    private final byte[] bytes;

    public BMPFile(byte [] bytes) {
        this.bytes = bytes;
    }
    
    public BMPFile(BMPFile other){
        this.bytes = other.bytes.clone();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getContentSize(){
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt(BMP_SIZE_INDEX_START) - BMP_HEADER_SIZE;
    }

    public String getSignature(){
        return "" + (char) bytes[0] + (char) bytes[1];
    }
}
