package ar.edu.itba.utils;

public class Arguments {

    //Embedding
    private final boolean embed;
    private final String inFile;
    private final String inBitMapFile;
    private final String outFile;
    private final String stegAlgorithm;
    private String encryptionAlgorithm;
    private String mode;
    private final String password;

    //Extracting
    private final boolean extract;


    private Arguments(Builder builder){
        this.embed = builder.embed;
        this.inFile = builder.inFile;
        this.inBitMapFile = builder.inBitMapFile;
        this.outFile = builder.outFile;
        this.stegAlgorithm = builder.stegAlgorithm;
        this.encryptionAlgorithm = builder.encryptionAlgorithm;
        this.mode = builder.mode;
        this.password = builder.password;

        this.extract = builder.extract;
    }

    public boolean isEmbed() {
        return embed;
    }

    public String getInFile() {
        return inFile;
    }

    public String getInBitMapFile() {
        return inBitMapFile;
    }

    public String getOutFile() {
        return outFile;
    }

    public String getStegAlgorithm() {
        return stegAlgorithm;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public String getMode() {
        return mode;
    }

    public String getPassword() {
        return password;
    }

    public boolean isExtract() {
        return extract;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public static class Builder {
        private boolean embed;
        private String inFile;
        private String inBitMapFile;
        private String outFile;
        private String stegAlgorithm;
        private String encryptionAlgorithm;
        private String mode;
        private String password;

        private boolean extract;

        public Builder embed(boolean embed){
            this.embed = embed;
            return this;
        }

        public Builder inFile(String inFile){
            this.inFile = inFile;
            return this;
        }

        public Builder inBitMapFile(String inBitMapFile){
            this.inBitMapFile = inBitMapFile;
            return this;
        }

        public Builder outFile(String outFile){
            this.outFile = outFile;
            return this;
        }

        public Builder stegAlgorithm(String stegAlgorithm){
            this.stegAlgorithm = stegAlgorithm;
            return this;
        }

        public Builder encryptionAlgorithm(String encryptionAlgorithm){
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public Builder mode(String mode){
            this.mode = mode;
            return this;
        }

        public Builder password(String password){
            this.password = password;
            return this;
        }

        public Builder extract(boolean extract){
            this.extract = extract;
            return this;
        }

        public Arguments build(){
            return new Arguments(this);
        }
    }

}
