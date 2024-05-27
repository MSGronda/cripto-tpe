package ar.edu.itba.stenography;

import ar.edu.itba.utils.BMPFile;

public interface LSBInterface {
    BMPFile hideFile(BMPFile inFile, byte[] fileToHide, int contentSize);

    byte[] obtainFile(BMPFile inFile);

    String getExtension(BMPFile inFile);
}
