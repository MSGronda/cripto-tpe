package ar.edu.itba.utils;

import java.util.List;
import java.util.stream.Stream;

public class Parser {

    public static Arguments parse(String[] args){
        Arguments.Builder builder = new Arguments.Builder();

        for (int i = 0; i < args.length; i++){
            switch (args[i]){
                case "-embed":
                    builder.embed(true);
                    break;
                case "-in":
                    // El proximo argumento se guarda
                    builder.inFile(args[++i]);
                    break;
                case "-p":
                    builder.inBitMapFile(args[++i]);
                    break;
                case "-out":
                    builder.outFile(args[++i]);
                    break;
                case "-steg":
                    builder.stegAlgorithm(args[++i]);
                    break;
                case "-a":
                    builder.encryptionAlgorithm(args[++i]);
                    break;
                case "-m":
                    builder.mode(args[++i]);
                    break;
                case "-pass":
                    builder.password(args[++i]);
                    break;
                case "-extract":
                    builder.extract(true);
                    break;
            }
        }
        return builder.build();
    }

    public static void checkParams(Arguments arguments) {
        if (!arguments.isEmbed() && !arguments.isExtract()){
            System.out.println("No embed nor extract option specified");
            System.exit(1);
        }
        if ( arguments.isEmbed()){
            if (arguments.getInFile() == null){
                System.out.println("No input file specified for embedding");
                System.exit(1);
            }
        }
        if (arguments.getInBitMapFile() == null){
            System.out.println("No input bitmap file specified");
            System.exit(1);
        }
        if( arguments.getOutFile() == null){
            System.out.println("No output file specified");
            System.exit(1);
        }
        if (arguments.getStegAlgorithm() == null){
            System.out.println("No steg algorithm specified");
            System.exit(1);
        }
        //Chequea que el algoritmo de esteganografia sea valido
        if (Stream.of("LSB1", "LSB4", "LSBI").noneMatch(arguments.getStegAlgorithm()::equals)){
            System.out.println("Invalid steg algorithm specified");
            System.exit(1);
        }
        // Chequea si se paso algoritmo de encripcion y/o modo pero no se paso password
        if ( (arguments.getEncryptionAlgorithm() != null || arguments.getMode() != null) && arguments.getPassword() == null){
            System.out.println("No password specified for encryption");
            System.exit(1);
        }
        // Si se paso password pero no algoritmo o modo, se setea el default
        if (arguments.getPassword() != null){
            if( arguments.getEncryptionAlgorithm() == null){
                arguments.setEncryptionAlgorithm("aes128");
            }else{
                //chequea que sea valido
                if (Stream.of("aes128", "aes192", "aes256", "des").noneMatch(arguments.getEncryptionAlgorithm()::equals)){
                    System.out.println("Invalid encryption algorithm specified");
                    System.exit(1);
                }
            }
            if ( arguments.getMode() == null){
                arguments.setMode("cbc");
            }
            else{
                //chequea que sea valido el modo
                if (Stream.of("cbc", "cfb", "ofb", "ecb").noneMatch(arguments.getMode()::equals)){
                    System.out.println("Invalid mode specified");
                    System.exit(1);
                }
            }
        }
    }
}
