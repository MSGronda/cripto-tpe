package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Util;
import static ar.edu.itba.utils.Util.*;
import static ar.edu.itba.utils.Util.BITS_IN_BYTE;

public class LSBImproved {
    private static final int NUM_PATTERNS = 4;
    private static final byte PATTERN_BITS = 0x6;   // 0000 0110
    private static final int PATTERN_1_IDX = 0;
    private static final byte PATTERN_1 = 0x0;      // 0000 0000
                                                    //       ^^
    private static final int PATTERN_2_IDX = 1;
    private static final byte PATTERN_2 = 0x2;      // 0000 0010
                                                    //       ^^
    private static final int PATTERN_3_IDX = 2;
    private static final byte PATTERN_3 = 0x4;      // 0000 0100
                                                    //       ^^
    private static final int PATTERN_4_IDX = 3;
    private static final byte PATTERN_4 = 0x6;      // 0000 0110
                                                    //       ^^
    private static final byte BLUE_BYTE = 0;
    private static final byte GREEN_BYTE = 1;
    private static final byte RED_BYTE = 2;

    private static final int LSB1_START_OFFSET = BMP_HEADER_SIZE + NUM_PATTERNS;

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){

        // Sumamos los NUM_PATTERNS por separado dado que bytesRequired ignora los rojos
        int bytesRequiredToHide = bytesRequired(LSB1_START_OFFSET, fileToHide.length + INT_SIZE) + NUM_PATTERNS;

        if(inFile.getContentSize() < bytesRequiredToHide){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        byte[] inBytes = inFile.getBytes();

        // Conseguimos las ocurrencias iniciales de los patrones
        int[] patternOccurrences = countPatternOccurrences(inBytes, LSB1_START_OFFSET, bytesRequiredToHide);

        BMPFile outFile = lsb1IgnoringRed(inFile, LSB1_START_OFFSET,  fileToHide, contentSize);
        byte[] outBytes = outFile.getBytes();

        // Contamos la cantidad de veces (para cada patron) que cambio el ultimo bit
        int[] bitChanged = checkBitsChanged(inBytes, outBytes);

        // Decidimos cuales de los patrones deberian invertirse y cuales no
        boolean[] inversions = calculateInversions(patternOccurrences, bitChanged);

        storeInversionBits(outBytes, inversions);

        // Invertimos solo los bits que tienen que ser invertidos (dentro del rango de bits invertidos)
        invertBits(outBytes, LSB1_START_OFFSET, bytesRequiredToHide, inversions);

        return outFile;
    }

    private static void storeInversionBits(byte[] outBytes, boolean[] inversion){
        for(int i=0; i<NUM_PATTERNS; i++){
            outBytes[i + BMP_HEADER_SIZE] &= (byte) (~0x1);
            outBytes[i + BMP_HEADER_SIZE] |= (byte) (inversion[i] ? 0x1 : 0x0);
        }
    }

    private static boolean[] calculateInversions(int[] patternOccurrences, int[] bitChanged){
        boolean[] inversions = new boolean[NUM_PATTERNS];
        for(int i=0; i<NUM_PATTERNS; i++){
            if(patternOccurrences[i] - bitChanged[i] < bitChanged[i]){
                inversions[i] = true;
            }
        }
        return inversions;
    }

    private static void invertBits(byte[] outBytes, int from, int to, boolean[] inversions){
        byte pos = byteColor(from);

        for(int i=to; i>=from; ){
            byte patternBits = (byte) (outBytes[i] & PATTERN_BITS);

            switch (patternBits){
                case PATTERN_1 -> invertBit(outBytes, i, inversions[PATTERN_1_IDX]);
                case PATTERN_2 -> invertBit(outBytes, i, inversions[PATTERN_2_IDX]);
                case PATTERN_3 -> invertBit(outBytes, i, inversions[PATTERN_3_IDX]);
                case PATTERN_4 -> invertBit(outBytes, i, inversions[PATTERN_4_IDX]);
                default -> throw new RuntimeException("No deberia ocurrir esto!");
            }

            if(pos == GREEN_BYTE){
                i--;
                pos = BLUE_BYTE;
            }
            else if(pos == BLUE_BYTE){
                i -= 2;             // Skipeo el rojo
                pos = GREEN_BYTE;
            }
            else{
                throw new RuntimeException("Esto no deberia ocurrir!");
            }

        }
    }
    private static void invertBit(byte[] outBytes, int i, boolean invert){
        if(invert){
            byte b = outBytes[i];
            outBytes[i] &= (byte) (~0x1);
            outBytes[i] |= (byte) (b & 0x1);
        }
    }

    private static int[] checkBitsChanged(byte[] inBytes, byte[] outBytes){
        int[] bitChanged = new int[NUM_PATTERNS];
        for(int i=0; i<inBytes.length; i++){
            byte patternBits = (byte) (inBytes[i] & PATTERN_BITS);

            switch (patternBits) {
                case PATTERN_1 -> checkBitChanged(inBytes, outBytes, i, bitChanged, PATTERN_1_IDX);
                case PATTERN_2 -> checkBitChanged(inBytes, outBytes, i, bitChanged, PATTERN_2_IDX);
                case PATTERN_3 -> checkBitChanged(inBytes, outBytes, i, bitChanged, PATTERN_3_IDX);
                case PATTERN_4 -> checkBitChanged(inBytes, outBytes, i, bitChanged, PATTERN_4_IDX);
                default -> throw new RuntimeException("No deberia ocurrir esto!");
            }
        }
        return bitChanged;
    }
    private static void checkBitChanged(byte[] inBytes, byte[] outBytes, int i, int[] bitChanged, int patternPos) {
        if((inBytes[i] & ~ 0x1) != (outBytes[i] & ~ 0x1)){
            bitChanged[patternPos]++;
        }
    }

    private static int[] countPatternOccurrences(byte[] inBytes, int from, int to){
        int[] patternOccurrences = new int[NUM_PATTERNS];

        byte pos = byteColor(from);

        for(int i = to; i>=from; ){
            byte patternBits = (byte) (inBytes[i] & PATTERN_BITS);

            switch (patternBits) {
                case PATTERN_1 -> patternOccurrences[PATTERN_1_IDX]++;
                case PATTERN_2 -> patternOccurrences[PATTERN_2_IDX]++;
                case PATTERN_3 -> patternOccurrences[PATTERN_3_IDX]++;
                case PATTERN_4 -> patternOccurrences[PATTERN_4_IDX]++;
                default -> throw new RuntimeException("No deberia ocurrir esto!");
            }

            if(pos == GREEN_BYTE){
                i--;
                pos = BLUE_BYTE;
            }
            else if(pos == BLUE_BYTE){
                i -= 2;             // Skipeo el rojo
                pos = GREEN_BYTE;
            }
            else{
                throw new RuntimeException("Esto no deberia ocurrir!");
            }

        }
        return patternOccurrences;
    }

    private static byte byteColor(int idx){
        return (byte) ((idx - BMP_HEADER_SIZE) % 3);
    }

    // = = = = = = = = LSB1 ignoring red = = = = = = = =

    private static BMPFile lsb1IgnoringRed(BMPFile inFile, int from, byte[] fileToHide, int contentSize){
        // El chequeo de tamaño lo hacemos afuera

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outByteOffset = from;

        // Ocultamos el tamaño del archivo (repetimos el codigo para no tener que expandir el array de fileToHide)
        for(byte b : Util.intToBytes(contentSize)){
            int bytesRequired = bytesRequired(outByteOffset, 1);

            hideByte(b, outBytes, outByteOffset, bytesRequired);

            outByteOffset += bytesRequired;
        }

        // Ocultamos contenido del archivo
        for (byte b : fileToHide) {
            int bytesRequired = bytesRequired(outByteOffset, 1);

            hideByte(b, outBytes, outByteOffset, bytesRequired);

            outByteOffset += bytesRequired;
        }
        return outFile;
    }

    private static void hideByte(byte byteToHide, byte[] outBytes, int outByteOffset, int bytesRequired){
        int pos = byteColor(outByteOffset);

        for (int j=bytesRequired; j>=0; ) {                // Voy de atras para adelante
            outBytes[outByteOffset + j] &= (byte) (~0x1);
            outBytes[outByteOffset + j] |= (byte) (byteToHide & 0x1);
            byteToHide >>>= 1;

            if(pos == GREEN_BYTE){
                j--;
                pos = BLUE_BYTE;
            }
            else if(pos == BLUE_BYTE){
                j -= 2;             // Skipeo el rojo
                pos = GREEN_BYTE;
            }
            else{
                throw new RuntimeException("Esto no deberia ocurrir!");
            }
        }
    }

    private static int bytesRequired(int idx, int numBytes){
        // Calcula la cantidad de bytes del outputFile se requieren para guardar numBytes.
        // No estamos usando el rojo pero igual lo contamos como si fuese parte.

        int bytesRequired = (4 + BITS_IN_BYTE) * numBytes;

        // Dependiendo del indice actual (si corresponde con uno rojo, azul o verde), tenemos que agregar/restar bytes.
        byte color = byteColor(idx);
        switch (color){
            case BLUE_BYTE -> bytesRequired -= 1;
            case GREEN_BYTE -> bytesRequired += 1;
            case RED_BYTE -> bytesRequired += 2;
        }

        return bytesRequired;
    }
}
