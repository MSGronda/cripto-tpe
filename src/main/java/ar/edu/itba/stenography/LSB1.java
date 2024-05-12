package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LSB1 {
    private static final int BITS_IN_BYTE = 8;
    private static final int BMP_HEADER_SIZE = 54;

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        for(int i=0; i<fileToHide.length; i++){
            for (int j = 0; j < BITS_IN_BYTE; j++) {
                byte bit = (byte) ((fileToHide[i] >> j) & 1);

                byte mask = (byte) (254 + bit);

                outBytes[BMP_HEADER_SIZE + BITS_IN_BYTE * i + j] = (byte) (outBytes[BMP_HEADER_SIZE + BITS_IN_BYTE * i + j] & mask);
            }
        }
        return outFile;
    }

    public static byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int fileSize = 0;

        // Para obtener tamaño del file escondido
        for (int i = BMP_HEADER_SIZE ; i < BMP_HEADER_SIZE + 4 * BITS_IN_BYTE; i++){
            byte bit = (byte) (inBytes[i] & 1);
            fileSize += bit;
            fileSize = fileSize << 1;
        }

        byte[] outBytes = new byte[fileSize];
        
        return outBytes;
    }


}
