package ar.edu.itba;
import ar.edu.itba.stenography.LSBInterface;
import ar.edu.itba.utils.Arguments;
import ar.edu.itba.stenography.LSB1;
import ar.edu.itba.stenography.LSB4;
import ar.edu.itba.stenography.LSBImproved;
import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Parser;
import ar.edu.itba.utils.Util;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.KeySpec;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

public class Main {

    private static final int ITERATION_COUNT = 10000;

    public static Cipher createCipher(Arguments arguments, Boolean encryptMode) throws Exception {
        String encryptionAlgorithm = arguments.getEncryptionAlgorithm();
        String mode = arguments.getMode();
        String password = arguments.getPassword();

        String keyAlgorithm;
        int keySize;
        switch (encryptionAlgorithm.toLowerCase()) {
            case "aes128":
                keyAlgorithm = "AES";
                keySize = 128;
                break;
            case "aes192":
                keyAlgorithm = "AES";
                keySize = 192;
                break;
            case "aes256":
                keyAlgorithm = "AES";
                keySize = 256;
                break;
            case "des":
                keyAlgorithm = "DES";
                keySize = 56;
                break;
            default:
                throw new IllegalArgumentException("Invalid encryption algorithm");
        }
        int ivSize = 16;
        int totalKeyMaterialLength = keySize + (ivSize * 8);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] salt = new byte[]{0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, totalKeyMaterialLength);
        SecretKey tmp = factory.generateSecret(keySpec);
        byte[] keyTotal = tmp.getEncoded();

        byte[] keyBytes = copyOf(keyTotal, keySize / 8);
        byte[] iv = copyOfRange(keyTotal, keySize / 8, keyTotal.length);

        SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
        IvParameterSpec IV = new IvParameterSpec(iv);

        String transformation = String.format("%s/%s/PKCS5Padding", keyAlgorithm, mode.toUpperCase());
        Cipher cipher = Cipher.getInstance(transformation);

        if (encryptMode){
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
        }
        return cipher;
    }

    private static LSBInterface getLSB(Arguments arguments){
        switch (arguments.getStegAlgorithm()){
            case "LSB1":
                return new LSB1();
            case "LSB4":
                return new LSB4();
            case "LSBI":
                return new LSBImproved();
            default:
                throw new IllegalArgumentException("Invalid LSB");
        }
    }

    public static String getExtensionFromPath(String path){
        int i = path.lastIndexOf('.');
        if (i > 0) {
            return path.substring(i + 1);
        }
        return "";
    }


    public static void main(String[] args) throws Exception {
        byte[] file;

        Arguments arguments = Parser.parse(args);
        Parser.checkParams(arguments);

        if (arguments.isEmbed()) {
            byte[] inFile = Files.readAllBytes(Path.of(arguments.getInFile()));
            int size = inFile.length;
            String extension = getExtensionFromPath(arguments.getInFile());

            BMPFile bmpFile = new BMPFile(Files.readAllBytes(Path.of(arguments.getInBitMapFile())));

            inFile = Util.withExtension(inFile, extension);


            // TODO - Revisar encripcion
            if (arguments.getEncryptionAlgorithm() != null){
                inFile = Util.addSizeInFront(inFile, size);
                Cipher cipher = createCipher(arguments, true);
                inFile = cipher.doFinal(inFile);
                size = inFile.length;
            }

            LSBInterface LSB = getLSB(arguments);

            BMPFile outFile = LSB.hideFile(bmpFile, inFile, size);

            outFile.dumpFile(arguments.getOutFile());

        } else if (arguments.isExtract()){
            BMPFile bmpFile = new BMPFile(Files.readAllBytes(Path.of(arguments.getInBitMapFile())));

            LSBInterface LSB = getLSB(arguments);

            byte[] hidden = LSB.obtainFile(bmpFile);

            String extension;

            // TODO - revisar encripcion
            if (arguments.getEncryptionAlgorithm() != null){
                Cipher cipher = createCipher(arguments, false);
                hidden = cipher.doFinal(hidden);

                // Se obtiene la data de lo descifrado
                int size = Util.getSize(hidden);
                hidden = Util.getData(hidden, size);
                extension = Util.getExtension(hidden, size);
            } else{
                extension = LSB.getExtension(bmpFile);
            }

            saveBytesToFile(hidden, arguments.getOutFile(), extension);
        }
    }

    public static void saveBytesToFile(byte[] bytes, String fileName, String fileExtension) throws IOException {
        // Construct the file path with the specified extension
        String filePath = fileName + "." + fileExtension;
        File file = new File(filePath);

        // Write the byte array to the file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
    }

}