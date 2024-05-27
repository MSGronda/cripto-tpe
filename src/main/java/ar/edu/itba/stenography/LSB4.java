package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import static ar.edu.itba.utils.Util.BITS_IN_BYTE;
import static ar.edu.itba.utils.Util.BMP_HEADER_SIZE;

public class LSB4 implements LSBInterface{
    private static final int FILE_SIZE = 0;
    private static final int OFFSET = 1;
    private static final int BITS_TO_HIDE = 4;
    private static final int INT_SIZE = Integer.BYTES * BITS_IN_BYTE / BITS_TO_HIDE; // 4 * 8 / 4 = 8

    @Override
    public BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE / BITS_TO_HIDE + INT_SIZE ){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outByteOffset = BMP_HEADER_SIZE;

        // Ocultamos el tamaño del archivo
        int size = contentSize;
        for( int i = INT_SIZE - 1; i >= 0; i-- ){
            outBytes[outByteOffset + i] &= (byte) (0xF0);
            outBytes[outByteOffset + i] |= (byte) (size & 0x0F);
            size >>>= BITS_TO_HIDE;
        }

        outByteOffset += INT_SIZE;

        // Ocultamos contenido del archivo
        for (byte b : fileToHide) {
            byte byteToHide = b;

            for (int j = BITS_IN_BYTE / BITS_TO_HIDE - 1; j >= 0; j--) {
                outBytes[outByteOffset + j] &= (byte) (0xF0);
                outBytes[outByteOffset + j] |= (byte) (byteToHide & 0x0F);
                byteToHide >>>= BITS_TO_HIDE;
            }

            outByteOffset += BITS_IN_BYTE / BITS_TO_HIDE;
        }

        return outFile;
    }

    @Override
    public byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int outBytesOffset = 0;

        // Obtenemos el tamaño de lo obtenido
        int[] sizeData = getFileSize(inFile);
        int fileSize = sizeData[FILE_SIZE];
        int inBytesOffset = sizeData[OFFSET];

        byte[] outBytes = new byte[fileSize];

        // Obtenemos el contenido del archivo
        for (int i = 0; i < fileSize; i++){
            byte byteToObtain = 0;

            for (int j = 0; j < BITS_IN_BYTE / BITS_TO_HIDE; j++){
                byteToObtain |= (byte) (inBytes[inBytesOffset] & 0x0F);
                inBytesOffset++;

                if(j < BITS_IN_BYTE / BITS_TO_HIDE - 1){
                    byteToObtain <<= BITS_TO_HIDE;
                }
            }
            outBytes[outBytesOffset] = byteToObtain;
            outBytesOffset++;
        }

        return outBytes;
    }

    @Override
    public String getExtension(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int[] size = getFileSize(inFile);
        int inBytesOffset = 2 * size[FILE_SIZE] + size[OFFSET];

        StringBuilder resp = new StringBuilder();

        byte extractedByte = 1;
        while(extractedByte != 0){

            extractedByte = 0;

            for (int j = 0; j < BITS_IN_BYTE / BITS_TO_HIDE; j++){
                extractedByte |= (byte) (inBytes[inBytesOffset] & 0x0F);
                inBytesOffset++;

                if(j < BITS_IN_BYTE / BITS_TO_HIDE - 1){
                    extractedByte <<= BITS_TO_HIDE;
                }
            }

            if( extractedByte != 0) {
                resp.append((char) extractedByte);
            }
        }

        return resp.toString();
    }

    private static int[] getFileSize(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();
        int inBytesOffset = BMP_HEADER_SIZE;

        int fileSize = 0;

        for (int i = 0 ; i < INT_SIZE; i++){
            fileSize |= (byte) (inBytes[inBytesOffset] & 0x0F);
            inBytesOffset++;
            if(i < INT_SIZE - 1){
                fileSize <<= BITS_TO_HIDE;
            }
        }

        return new int[]{fileSize, inBytesOffset};
    }
}
