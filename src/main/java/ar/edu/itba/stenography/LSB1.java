package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import static ar.edu.itba.utils.Util.*;

public class LSB1 implements LSBInterface{
    private static final int FILE_SIZE = 0;
    private static final int OFFSET = 1;

    @Override
    public BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE + INT_BIT_SIZE){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outByteOffset = BMP_HEADER_SIZE;

        // Ocultamos el tamaño del archivo
        int size = contentSize;
        for(int i = INT_BIT_SIZE -1; i>=0; i--){                               // Voy de atras para adelante
            outBytes[outByteOffset + i] &= (byte) (~0x1);
            outBytes[outByteOffset + i] |= (byte) (size & 0x1);
            size >>>= 1;
        }

        outByteOffset += INT_BIT_SIZE;

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


    @Override
    public byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int outBytesOffset = 0;

        // Obtenemos el tamaño de lo obtenido
        int[] sizeData = getFileSize(inFile);
        int fileSize = sizeData[FILE_SIZE];
        int inBytesOffset = sizeData[OFFSET];

        if(fileSize > inBytes.length - (BMP_HEADER_SIZE + INT_BIT_SIZE) || fileSize <= 0) {
            throw new RuntimeException("Tamaño invalido de archivo (" + fileSize + ")");
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

    @Override
    public String getExtension(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int[] size = getFileSize(inFile);
        int inBytesOffset = 8 * size[FILE_SIZE] + size[OFFSET];

        StringBuilder resp = new StringBuilder();

        byte extractedByte = 1;
        while(extractedByte != 0){

            extractedByte = 0;

            for(int j=0; j < BITS_IN_BYTE; j++){
                extractedByte |= (byte) (inBytes[inBytesOffset] & 0x1);
                inBytesOffset++;

                if(j < BITS_IN_BYTE - 1){
                    extractedByte <<= 1;
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

        for (int i = 0; i < INT_BIT_SIZE; i++){
            fileSize |= (byte) (inBytes[inBytesOffset] & 0x1);
            inBytesOffset++;

            if(i< INT_BIT_SIZE -1){
                fileSize <<= 1;
            }
        }
        return new int[]{fileSize, inBytesOffset};
    }
}
