package ar.edu.itba.utils;

public class Util {
    public static byte[] withExtension(byte[] infile, String extension){
        byte[] resp = new byte[infile.length + extension.length() + 1];

        System.arraycopy(infile, 0, resp, 0, infile.length);
        System.arraycopy(extension.getBytes(), 0, resp, infile.length, extension.length());
        resp[resp.length - 1] = 0;

        return resp;
    }
}
