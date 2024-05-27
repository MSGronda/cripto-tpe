package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Util;

import static ar.edu.itba.utils.Util.*;
import static ar.edu.itba.utils.Util.BITS_IN_BYTE;

public class LSBImproved {
    private static final int NUM_PATTERNS = 4;
    private static final byte PATTERN_BITS = 0x6;   // 0000 0110
    private static final byte PATTERN_1 = 0x0;      // 0000 0000
                                                    //       ^^
    private static final byte PATTERN_2 = 0x2;      // 0000 0010
                                                    //       ^^
    private static final byte PATTERN_3 = 0x4;      // 0000 0100
                                                    //       ^^
    private static final byte PATTERN_4 = 0x6;      // 0000 0110
                                                    //       ^^

    public static BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){

        byte[] inBytes = inFile.getBytes();

        // Conseguimos las ocurrencias iniciales de los patrones
        int[] patternOccurrences = countPatternOccurrences(inBytes);

        BMPFile outFile = lsb1IgnoringRed(inFile, fileToHide, contentSize);

        // Conseguimos las ocurrencias de los patrones luego de aplicar LSB1
        // TODO: preguntar porque tenemos que recalcular esto si no va a haber cambiado
        // int[] patternOccurrencesPostLSB1 = countPatternOccurrences(outFile.getBytes());

        // Contamos la cantidad de veces (para cada patron) que cambio el ultimo bit
        byte[] outBytes = outFile.getBytes();
        int[] bitChanged = new int[NUM_PATTERNS];
        for(int i=0; i<inBytes.length; i++){
            byte patternBits = (byte) (inBytes[i] & PATTERN_BITS);

            switch (patternBits) {
                case PATTERN_1 -> checkBitChanged(inBytes, outBytes, i, bitChanged, 0);
                case PATTERN_2 -> checkBitChanged(inBytes, outBytes, i, bitChanged, 1);
                case PATTERN_3 -> checkBitChanged(inBytes, outBytes, i, bitChanged, 2);
                case PATTERN_4 -> checkBitChanged(inBytes, outBytes, i, bitChanged, 3);
                default -> throw new RuntimeException("No deberia ocurrir esto!");
            }
        }

        // Decidimos cuales de los patrones deberian invertirse y cuales no
        byte[] inversions = new byte[NUM_PATTERNS];
        for(int i=0; i<NUM_PATTERNS; i++){
            if(patternOccurrences[i] - bitChanged[i] < bitChanged[i]){
                inversions[i] = 1;
            }
        }

        // TODO: invert bytes


        return outFile;
    }



    private static void checkBitChanged(byte[] inBytes, byte[] outBytes, int i, int[] bitChanged,int patternPos) {
        if((inBytes[i] & ~ 0x1) != (outBytes[i] & ~ 0x1)){
            bitChanged[patternPos]++;
        }
    }

    private static int[] countPatternOccurrences(byte[] inBytes){
        int[] patternOccurrences = new int[NUM_PATTERNS];

        for(int i = Util.BMP_HEADER_SIZE; i<inBytes.length; i++){
            byte patternBits = (byte) (inBytes[i] & PATTERN_BITS);

            switch (patternBits) {
                case PATTERN_1 -> patternOccurrences[0]++;
                case PATTERN_2 -> patternOccurrences[1]++;
                case PATTERN_3 -> patternOccurrences[2]++;
                case PATTERN_4 -> patternOccurrences[3]++;
                default -> throw new RuntimeException("No deberia ocurrir esto!");
            }
        }
        return patternOccurrences;
    }


    // = = = = = = = = LSB1 ignoring red = = = = = = = =

    private static final byte BLUE_BYTE = 0;
    private static final byte GREEN_BYTE = 1;
    private static final byte RED_BYTE = 2;
    private static byte byteColor(int idx){
        return (byte) ((idx - BMP_HEADER_SIZE) % 3);
    }
    public static int bytesRequired(int idx, int numBytes){
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

    public static BMPFile lsb1IgnoringRed(BMPFile inFile, byte[] fileToHide, int contentSize){
        if(inFile.getContentSize() < bytesRequired(BMP_HEADER_SIZE, fileToHide.length + INT_SIZE)){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        BMPFile outFile = new BMPFile(inFile);
        byte[] outBytes = outFile.getBytes();

        int outByteOffset = BMP_HEADER_SIZE;

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


}
