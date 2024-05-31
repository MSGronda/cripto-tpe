package ar.edu.itba.utils;

public class Util {
    public static final int INT_SIZE = 4;
    public static final int BITS_IN_BYTE = 8;
    public static final int INT_BIT_SIZE = Integer.BYTES * BITS_IN_BYTE;

    public static final int BMP_HEADER_SIZE = 54;

    public static byte[] withExtension(byte[] infile, String extension){
        byte[] resp = new byte[infile.length + extension.length() + 1];

        System.arraycopy(infile, 0, resp, 0, infile.length);
        System.arraycopy(extension.getBytes(), 0, resp, infile.length, extension.length());
        resp[resp.length - 1] = 0;

        return resp;
    }

    public static byte[] intToBytes(int value){
        byte[] resp = new byte[INT_SIZE];

        for(int i=0; i<INT_SIZE; i++){
            resp[INT_SIZE - i - 1] =  (byte) (value & 0xFF);
            value >>= BITS_IN_BYTE;
        }

        return resp;
    }

    public static byte[] addSizeInFront(byte[] infile, int size){
        byte[] resp = new byte[infile.length + INT_SIZE];

        byte[] sizeBytes = intToBytes(size);

        System.arraycopy(sizeBytes, 0, resp, 0, INT_SIZE);
        System.arraycopy(infile, 0, resp, INT_SIZE, infile.length);

        return resp;
    }


    // Por si se encripto
    public static int getSize(byte[] bytes){
        int size = 0;

        for(int i=0; i<INT_SIZE; i++){
            size |= (bytes[i] & 0xFF) << (i * BITS_IN_BYTE);
        }

        return size;
    }

    public static byte[] getData(byte[] bytes, int size){
        byte[] resp = new byte[size];

        System.arraycopy(bytes, INT_SIZE, resp, 0, size);

        return resp;
    }

    public static String getExtension(byte[] bytes, int size){
        StringBuilder resp = new StringBuilder();

        byte extractedByte = 1;
        int offset = INT_SIZE + size;
        while(extractedByte != 0){
            extractedByte = 0;

            for (int j = 0; j < BITS_IN_BYTE; j++){
                extractedByte |= (byte) (bytes[offset] & 0x1);
                offset++;

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
}
