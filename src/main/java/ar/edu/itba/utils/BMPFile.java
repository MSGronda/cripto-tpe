package ar.edu.itba.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void dumpFile(String path){
        File outputFile = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(this.bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
