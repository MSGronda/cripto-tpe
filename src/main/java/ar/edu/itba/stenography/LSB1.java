package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LSB1 {
    private static final int BITS_IN_BYTE = 8;
    private static final int INT_SIZE = Integer.BYTES * BITS_IN_BYTE;
    private static final int BMP_HEADER_SIZE = 54;

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide, String fileExtension){
        if(inFile.getContentSize() < fileToHide.length * BITS_IN_BYTE + INT_SIZE + (fileExtension.length() + 1) * BITS_IN_BYTE){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int offset = BMP_HEADER_SIZE;       // TODO: podriamos siempre usar el offset para escribir a outBytes

        // Ocultamos el tamaño del archivo
        int size = fileToHide.length;
        for(int i=0; i<INT_SIZE; i++){
            outBytes[offset + i] |= (byte) (size & 1);
            size >>= 1;
        }

        offset += INT_SIZE;

        // Ocultamos contenido del archivo
        for(int i=0; i<fileToHide.length; i++){
            byte byteToHide = fileToHide[i];

            for (int j = 0; j < BITS_IN_BYTE; j++) {
                outBytes[offset + BITS_IN_BYTE * i + j] |= (byte) (byteToHide & 1);
                byteToHide >>= 1;
            }
        }

        offset += fileToHide.length * BITS_IN_BYTE;

        // Ocultamos la extension
        byte[] fileExtensionBytes = generateExtensionArray(fileExtension);

        for(int i=0; i<fileExtension.length(); i++){
            byte byteToHide = fileExtensionBytes[i];

            for (int j = 0; j < BITS_IN_BYTE; j++) {
                outBytes[offset + BITS_IN_BYTE * i + j] |= (byte) (byteToHide & 1);
                byteToHide >>= 1;
            }
        }

        return outFile;
    }


    public static byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        int offset = BMP_HEADER_SIZE;

        // Para obtener tamaño del file escondido
        int fileSize = 0;
        for (int i = offset ; i < offset + INT_SIZE; i++){
            fileSize += (byte) (inBytes[i] & 1);
            fileSize <<= 1;
        }
        byte[] outBytes = new byte[fileSize];

        // TODO: complete

        return outBytes;
    }


    private static byte[] generateExtensionArray(String fileExtension) {
        byte[] fileExtensionBytes = fileExtension.getBytes();
        byte[] resp = new byte[fileExtensionBytes.length + 1];

        System.arraycopy(fileExtensionBytes, 0, resp, 0, fileExtensionBytes.length);

        resp[resp.length - 1] = '\0';

        return resp;
    }

}
