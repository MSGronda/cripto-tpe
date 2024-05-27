package ar.edu.itba;

import ar.edu.itba.stenography.LSB1;
import ar.edu.itba.stenography.LSB4;
import ar.edu.itba.stenography.LSBImproved;
import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        byte[] file;

//        Arguments arguments = Parser.parse(args);
//        Parser.checkParams(arguments);

        try {
            file = Files.readAllBytes(Path.of("./images/test.bmp"));
        } catch (IOException e){
            System.out.println(e);
            return;
        }
        byte[] infile = "asdofoasdjfojasf".getBytes();

        BMPFile outfile = LSBImproved.hideFile(new BMPFile(file), Util.withExtension(infile, ".jpg"), infile.length);

        System.out.println(outfile.getBytes().length);

    }



}