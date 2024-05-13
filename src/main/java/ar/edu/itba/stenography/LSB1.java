package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LSB1 {
    private static final int BITS_IN_BYTE = 8;
    private static final int INT_SIZE = Integer.BYTES * BITS_IN_BYTE;
    private static final int BMP_HEADER_SIZE = 54;

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE + INT_SIZE){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outBytesPosition = BMP_HEADER_SIZE;

        // Ocultamos el tamaño del archivo
        int size = fileToHide.length;
        for(int i=0; i<INT_SIZE; i++){
            outBytes[outBytesPosition] &= (byte) (~1 + (size & 1));
            size >>= 1;

            outBytesPosition++;
        }

        // Ocultamos contenido del archivo
        for(int i=0; i<fileToHide.length; i++){
            byte byteToHide = fileToHide[i];

            for (int j = 0; j < BITS_IN_BYTE; j++) {
                outBytes[outBytesPosition] &= (byte) (~1 + (byteToHide & 1));
                byteToHide >>= 1;

                outBytesPosition++;
            }
        }
        return outFile;
    }


    public static byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int inBytesOffset = BMP_HEADER_SIZE;
        int outBytesOffset = 0;

        // Para obtener tamaño del file escondido
        int fileSize = 0;
        for (int i = 0 ; i < INT_SIZE; i++){
            fileSize += (byte) (inBytes[inBytesOffset] & 1);
            fileSize <<= 1;

            inBytesOffset++;
        }
        byte[] outBytes = new byte[fileSize];

        // TODO: complete

        return outBytes;
    }

}
