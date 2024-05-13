package ar.edu.itba;

import ar.edu.itba.stenography.LSB4;
import ar.edu.itba.utils.Arguments;
import ar.edu.itba.utils.Parser;
import ar.edu.itba.stenography.LSB1;
import ar.edu.itba.utils.BMPFile;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
        byte[] infile = "asdofoasdjfojasf".getBytes();
        BMPFile outfile = LSB4.hideFile(new BMPFile(file), infile, infile.length);

        byte[] hidden = LSB4.obtainFile(outfile);
        System.out.println(Arrays.toString(infile));
        System.out.println(Arrays.toString(hidden));
    }



}