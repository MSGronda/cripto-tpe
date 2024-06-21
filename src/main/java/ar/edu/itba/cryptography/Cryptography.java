package ar.edu.itba.cryptography;

import ar.edu.itba.utils.Arguments;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

import static ar.edu.itba.utils.Util.BITS_IN_BYTE;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

public class Cryptography {
    private static final int ITERATION_COUNT = 10000;
    private static final int SALT_SIZE = 8;

    public static Cipher createCipher(Arguments arguments, int optMode, boolean withPadding) throws Exception {
        String encryptionAlgorithm = arguments.getEncryptionAlgorithm();
        String mode = getMode(arguments.getMode());
        String password = arguments.getPassword();

        String keyAlgorithm;
        int keySize;
        int ivSize;
        switch (encryptionAlgorithm.toLowerCase()) {
            case "aes128":
                keyAlgorithm = "AES";
                keySize = 128;
                ivSize = 16;
                break;
            case "aes192":
                keyAlgorithm = "AES";
                keySize = 192;
                ivSize = 16;
                break;
            case "aes256":
                keyAlgorithm = "AES";
                keySize = 256;
                ivSize = 16;
                break;
            case "des":
                keyAlgorithm = "TripleDES";
                keySize = 192;
                ivSize = 8;
                break;
            default:
                throw new IllegalArgumentException("Invalid encryption algorithm");
        }
        int totalKeyMaterialLength = keySize + (ivSize * BITS_IN_BYTE);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] salt = new byte[SALT_SIZE];      // Se inicializa en 0

        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, totalKeyMaterialLength);
        byte[] keyTotal = factory.generateSecret(keySpec).getEncoded();

        byte[] keyBytes = copyOf(keyTotal, keySize / BITS_IN_BYTE);
        byte[] iv = copyOfRange(keyTotal, keySize / BITS_IN_BYTE, keyTotal.length);

        SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
        IvParameterSpec IV = new IvParameterSpec(iv);

        String transformation = withPadding ? String.format("%s/%s/PKCS5Padding", keyAlgorithm, mode) : String.format("%s/%s/NoPadding", keyAlgorithm, mode);

        Cipher cipher = Cipher.getInstance(transformation);

        cipher.init(optMode, secretKey, IV);

        return cipher;
    }

    private static String getMode(String mode){
        return switch (mode) {
            case "cbc" -> "CBC";
            case "cfb" -> "CFB8";
            case "ofb" -> "OFB";
            case "ecb" -> "ECB";
            default -> throw new RuntimeException("No deberia ocurrir esto!");
        };
    }
}
