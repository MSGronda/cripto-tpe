package ar.edu.itba;

import ar.edu.itba.utils.Arguments;
import ar.edu.itba.utils.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {




    public static void main(String[] args) {
        byte[] file;

        Arguments arguments = Parser.parse(args);
        Parser.checkParams(arguments);

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