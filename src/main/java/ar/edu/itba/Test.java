package ar.edu.itba;

import ar.edu.itba.stenography.LSB1;
import ar.edu.itba.stenography.LSBImproved;
import ar.edu.itba.utils.BMPFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Test {
    public static void main(String[] args){
        byte[] file;
        LSBImproved lsbImproved = new LSBImproved();

        try {
            file = Files.readAllBytes(Path.of("./images/test.bmp"));
        } catch (IOException e){
            System.out.println(e);
            return;
        }

        BMPFile inFile = new BMPFile(file);
        byte[] fileToHide = "Tasdjfkasjdflasjdfajsdkfasl;dfjkasdf".getBytes();

        BMPFile outFile = lsbImproved.hideFile(inFile, fileToHide, fileToHide.length);

        byte[] outBytes = lsbImproved.obtainFile(outFile);

        System.out.println(Arrays.toString(fileToHide));
        System.out.println(fileToHide.length);
        System.out.println(Arrays.toString(outBytes));
        System.out.println(outBytes.length);

    }
}
