package ar.edu.itba;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {




    public static void main(String[] args) {
        byte[] file;

        try {
            file = Files.readAllBytes(Path.of("./images/test.bmp"));
        } catch (IOException e){
            System.out.println(e);
            return;
        }

        ByteBuffer buffer =  ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);

        System.out.println((char) file[0] + (char) file[1]);
        System.out.println(buffer.getInt(2));


    }



}