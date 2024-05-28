package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;
import ar.edu.itba.utils.Util;
import static ar.edu.itba.utils.Util.*;
import static ar.edu.itba.utils.Util.BITS_IN_BYTE;

public class LSBImproved implements LSBInterface {
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

    @Override
    public BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize){

        // Sumamos los NUM_PATTERNS por separado dado que bytesRequired ignora los rojos
        int bytesRequiredToHide = bytesRequired(LSB1_START_OFFSET, fileToHide.length + INT_SIZE) + NUM_PATTERNS;

        if(inFile.getContentSize() < bytesRequiredToHide){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        byte[] inBytes = inFile.getBytes();

        // Conseguimos las ocurrencias iniciales de los patrones
        int[] patternOccurrences = countPatternOccurrences(inBytes, LSB1_START_OFFSET, bytesRequiredToHide - NUM_PATTERNS); // Solo tenemos en cuenta los bytes afectados por el LSB1

        BMPFile outFile = hideLSB1IgnoringRed(inFile, LSB1_START_OFFSET,  fileToHide, contentSize);
        byte[] outBytes = outFile.getBytes();

        // Contamos la cantidad de veces (para cada patron) que cambio el ultimo bit
        int[] bitChanged = countBitsChanged(inBytes, outBytes, LSB1_START_OFFSET, bytesRequiredToHide - NUM_PATTERNS); // Solo tenemos en cuenta los bytes afectados por el LSB1

        // Decidimos cuales de los patrones deberian invertirse y cuales no
        boolean[] inversions = calculateInversions(patternOccurrences, bitChanged);

        storeInversionBits(outBytes, inversions);

        // Invertimos solo los bits que tienen que ser invertidos (dentro del rango de bits invertidos)
        invertBits(outBytes, LSB1_START_OFFSET, bytesRequiredToHide, inversions);

        return outFile;
    }

    @Override
    public byte[] obtainFile(BMPFile inFile){
        byte[] inBytes = inFile.getBytes();

        boolean[] inversions = getInversions(inBytes);

        return obtainLSB1IgnoringRedWithInversions(inBytes, inversions);    // La verdad no me gusto como quedo modularizado pero bueno
    }

    @Override
    public String getExtension(BMPFile inFile) {
        // TODO: complete
        return null;
    }

    // = = = = = = = = Auxiliary methods for Obtain = = = = = = = =
    private static boolean[] getInversions(byte[] inBytes) {
        boolean[] inversions = new boolean[NUM_PATTERNS];

        for(int i=0; i<NUM_PATTERNS; i++){
            if((inBytes[BMP_HEADER_SIZE + i] & 0x1) == 1){
                inversions[i] = true;
            }
        }

        return inversions;
    }

    // = = = = = = = = Auxiliary methods for Hide = = = = = = = =

    private static int getPatternIdx(byte inByte){
        byte patternBits = (byte) (inByte & PATTERN_BITS);

        return switch (patternBits){
            case PATTERN_1 -> PATTERN_1_IDX;
            case PATTERN_2 -> PATTERN_2_IDX;
            case PATTERN_3 -> PATTERN_3_IDX;
            case PATTERN_4 -> PATTERN_4_IDX;
            default -> throw new RuntimeException("No deberia ocurrir esto!");
        };
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
        byte pos = byteColor(to - 1);

        for(int i = to - 1; i>=from; ){

            // Invertimos el bit si es necesario
            if(inversions[getPatternIdx(outBytes[i])]){
                byte b = outBytes[i];
                outBytes[i] &= (byte) (~0x1);
                outBytes[i] |= (byte) (b & 0x1);
            }


            // Pasamos al proximo byte valido
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

    private static int[] countBitsChanged(byte[] inBytes, byte[] outBytes, int from, int to){
        int[] bitChanged = new int[NUM_PATTERNS];
        for(int i=from; i<to; i++){

            // Nos fijamos si cambio el bit. Si es el caso, sumamos otra ocurrencia para ese patron
            if((inBytes[i] & 0x1) != (outBytes[i] & 0x1)){
                bitChanged[getPatternIdx(inBytes[i])]++;
            }
        }
        return bitChanged;
    }
    private static int[] countPatternOccurrences(byte[] inBytes, int from, int to){
        int[] patternOccurrences = new int[NUM_PATTERNS];

        byte pos = byteColor(to - 1);

        for(int i = to - 1; i>=from; ){
            patternOccurrences[getPatternIdx(inBytes[i])]++;

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

    private static BMPFile hideLSB1IgnoringRed(BMPFile inFile, int from, byte[] fileToHide, int contentSize){
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
        int pos = byteColor(outByteOffset + bytesRequired - 1);

        for (int j=bytesRequired - 1; j>=0; ) {                // Voy de atras para adelante
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
        if(color == BLUE_BYTE){
            bytesRequired -= 1;
        }

        return bytesRequired;
    }

    private static byte[] obtainLSB1IgnoringRedWithInversions(byte[] inBytes, boolean[] inversions){
        // Obtenemos el tamaño
        int fileSizeOffset = bytesRequired(LSB1_START_OFFSET, INT_SIZE);
        int fileSize = getFileSize(inBytes, LSB1_START_OFFSET, fileSizeOffset);

        if(inBytes.length < bytesRequired(LSB1_START_OFFSET + INT_SIZE, fileSize)){
            throw new RuntimeException("No tenes suficiente espacio paaaa");
        }

        byte[] outBytes = new byte[fileSize];
        int inBytesOffset = LSB1_START_OFFSET + fileSizeOffset;

        // Obtenemos el color inicial del cursor (y si es rojo, pasamos a un color valido)
        int pos = byteColor(inBytesOffset);
        if(pos == RED_BYTE){
            pos = BLUE_BYTE;
            inBytesOffset++;
        }

        // Extraemos los bytes
        for(int outBytesOffset=0; outBytesOffset<fileSize; outBytesOffset++){
            byte extractedByte = 0;

            for(int j=0; j < BITS_IN_BYTE; j++){

                // Extraemos el byte, aplicando la inversion cuando sea necesaria
                byte inByte = inBytes[inBytesOffset];
                extractedByte |= inversions[getPatternIdx(inByte)] ? (byte) (~(inByte & 0x1)) : (byte) (inByte & 0x1);

                if(j < BITS_IN_BYTE - 1){
                    extractedByte <<= 1;
                }

                // Pasamos al proximo byte de azul/verde
                if(pos == BLUE_BYTE){
                    pos = GREEN_BYTE;
                    inBytesOffset++;
                }
                else if(pos == GREEN_BYTE){
                    pos = BLUE_BYTE;
                    inBytesOffset += 2;
                }
                else{
                    throw new RuntimeException("Esto no deberia ocurrir!");
                }
            }

            outBytes[outBytesOffset] = extractedByte;
            outBytesOffset++;
        }

        return outBytes;
    }

    private static int getFileSize(byte[] inBytes, int from, int to){

        int fileSize = 0;

        int pos = byteColor(to - 1);
        for (int i = to - 1; i >= from; ){
            fileSize |= (byte) (inBytes[i] & 0x1);

            if(i < INT_BIT_SIZE -1){
                fileSize <<= 1;
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
        return fileSize;
    }

}
