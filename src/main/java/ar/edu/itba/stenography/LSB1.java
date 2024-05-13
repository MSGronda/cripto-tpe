package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LSB1 {
    private static final int BITS_IN_BYTE = 8;
    private static final int INT_SIZE = Integer.BYTES * BITS_IN_BYTE;
    private static final int BMP_HEADER_SIZE = 54;

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE + INT_SIZE){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outByteOffset = BMP_HEADER_SIZE;

        // Ocultamos el tamaño del archivo
        int size = contentSize;
        for(int i=INT_SIZE-1; i>=0; i--){                               // Voy de atras para adelante
            outBytes[outByteOffset + i] &= (byte) (~0x1);
            outBytes[outByteOffset + i] |= (byte) (size & 0x1);
            size >>>= 1;
        }

        outByteOffset += INT_SIZE;

        // Ocultamos contenido del archivo
        for (byte b : fileToHide) {
            byte byteToHide = b;

            for (int j = BITS_IN_BYTE - 1; j >= 0; j--) {                // Voy de atras para adelante
                outBytes[outByteOffset + j] &= (byte) (~0x1);
                outBytes[outByteOffset + j] |= (byte) (byteToHide & 0x1);
                byteToHide >>>= 1;
            }

            outByteOffset += BITS_IN_BYTE;
        }
        return outFile;
    }


    public static byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int inBytesOffset = BMP_HEADER_SIZE;
        int outBytesOffset = 0;

        // Obtenemos el tamaño de lo obtenido
        int fileSize = 0;
        for (int i = 0 ; i < INT_SIZE; i++){
            fileSize |= (byte) (inBytes[inBytesOffset] & 0x1);
            inBytesOffset++;

            if(i<INT_SIZE-1){
                fileSize <<= 1;
            }
        }

        if(fileSize > inBytes.length - (BMP_HEADER_SIZE + INT_SIZE)){
            throw new RuntimeException("No dan los numeros");
        }

        byte[] outBytes = new byte[fileSize];

        // Obtenemos los datos
        for (int i=0; i < fileSize; i++){
            byte extractedByte = 0;

            for(int j=0; j < BITS_IN_BYTE; j++){
                extractedByte |= (byte) (inBytes[inBytesOffset] & 0x1);
                inBytesOffset++;

                if(j < BITS_IN_BYTE - 1){
                    extractedByte <<= 1;
                }
            }

            outBytes[outBytesOffset] = extractedByte;
            outBytesOffset++;
        }

        return outBytes;
    }

}
