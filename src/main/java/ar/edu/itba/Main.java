package ar.edu.itba;
import ar.edu.itba.cryptography.Cryptography;
import ar.edu.itba.stenography.LSBInterface;
import ar.edu.itba.utils.Arguments;
import ar.edu.itba.stenography.LSB1;
import ar.edu.itba.stenography.LSB4;
import ar.edu.itba.stenography.LSBImproved;
import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Parser;
import ar.edu.itba.utils.Util;
import javax.crypto.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {

    public static void main(String[] args) throws Exception {
        Arguments arguments = Parser.checkParams(Parser.parse(args));

        if (arguments.isEmbed()) {
           embedInImage(arguments);
        } else if (arguments.isExtract()){
            extractFromImage(arguments);
        }
    }

    private static void embedInImage(Arguments arguments) throws Exception {
        byte[] inFile = Files.readAllBytes(Path.of(arguments.getInFile()));
        int size = inFile.length;

        BMPFile bmpFile = new BMPFile(Files.readAllBytes(Path.of(arguments.getInBitMapFile())));
        inFile = Util.withExtension(inFile, getExtensionFromPath(arguments.getInFile()));

        // TODO - Revisar encripcion
        if (arguments.getEncryptionAlgorithm() != null){
            inFile = Util.addSizeInFront(inFile, size);
            Cipher cipher = Cryptography.createCipher(arguments, Cipher.ENCRYPT_MODE, false);
            inFile = cipher.doFinal(inFile);
            size = inFile.length;
        }

        LSBInterface LSB = getLSB(arguments);

        BMPFile outFile = LSB.hideFile(bmpFile, inFile, size);

        outFile.dumpFile(arguments.getOutFile());
    }

    private static void extractFromImage(Arguments arguments) throws Exception {
        BMPFile bmpFile = new BMPFile(Files.readAllBytes(Path.of(arguments.getInBitMapFile())));

        LSBInterface LSB = getLSB(arguments);

        byte[] hidden = LSB.obtainFile(bmpFile);

        String extension;

        // TODO - revisar encripcion
        if (arguments.getEncryptionAlgorithm() != null){
            Cipher cipher = Cryptography.createCipher(arguments, Cipher.DECRYPT_MODE, false);
            hidden = cipher.doFinal(hidden);

            int size = Util.getSize(hidden);

            if(size > hidden.length || size <= 0) {
                throw new RuntimeException("Invalid size in deciphered message (" + size + ")");
            }

            extension = Util.getExtension(hidden, size);
            hidden = Util.getData(hidden, size);
        } else{
            extension = LSB.getExtension(bmpFile);
        }

        saveBytesToFile(hidden, arguments.getOutFile(), extension);
    }


    private static LSBInterface getLSB(Arguments arguments){
        return switch (arguments.getStegAlgorithm()) {
            case "LSB1" -> new LSB1();
            case "LSB4" -> new LSB4();
            case "LSBI" -> new LSBImproved();
            default -> throw new IllegalArgumentException("Invalid LSB");
        };
    }

    private static String getExtensionFromPath(String path){
        int i = path.lastIndexOf('.');
        if (i > 0) {
            return path.substring(i);
        }
        return "";
    }

    private static void saveBytesToFile(byte[] bytes, String fileName, String fileExtension) throws IOException {
        // Construct the file path with the specified extension
        String filePath = fileName + fileExtension;
        File file = new File(filePath);

        // Write the byte array to the file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
    }
}